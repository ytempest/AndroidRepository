/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package okhttp3.internal.http;

import java.io.IOException;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.Version;
import okio.GzipSource;
import okio.Okio;

import static okhttp3.internal.Util.hostHeader;

/**
 * Description：为用户构建可以连接网络的 Request，为Request添加一些请求头，如：Host、Connection、Cookie
 * 如果还有RequestBody，还会添加 Content-Type、Content-Length等
 */
public final class BridgeInterceptor implements Interceptor {
    private final CookieJar cookieJar;

    public BridgeInterceptor(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

         /*---------- 这里开始处理Request ----------*/
        Request userRequest = chain.request();

        // 重新构建上一个拦截器传递过来的 Request，为这个Request添加一些请求头
        Request.Builder requestBuilder = userRequest.newBuilder();

        RequestBody body = userRequest.body();
        // 1、判断是否有 RequestBody，如果有就为这个RequestBody添加Content-Type头信息，并根据请求体的长度
        // 添加 Content-Length 或者 Transfer-Encoding:chunked 头信息
        if (body != null) {
            MediaType contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header("Content-Type", contentType.toString());
            }

            // 根据RequestBody的长度添加相应的头信息：
            // （1）如果请求体长度已知，那么添加 Content-Length头信息，并移除Transfer-Encoding:chunked头信息
            // （2）如果请求体长度未知，即为-1，那么添加 Transfer-Encoding:chunked头信息，并移除Content-Length头信息
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header("Content-Length", Long.toString(contentLength));
                requestBuilder.removeHeader("Transfer-Encoding");
            } else {
                requestBuilder.header("Transfer-Encoding", "chunked");
                requestBuilder.removeHeader("Content-Length");
            }
        }

        // 2、添加 Host头信息
        if (userRequest.header("Host") == null) {
            requestBuilder.header("Host", hostHeader(userRequest.url(), false));
        }

        // 3、添加 Connection:keep-alive 头信息
        if (userRequest.header("Connection") == null) {
            requestBuilder.header("Connection", "Keep-Alive");
        }

        // If we add an "Accept-Encoding: gzip" header field we're responsible for also decompressing
        // the transfer stream.
        // 4、如果还没有添加Accept-Encoding: gzip 和Range 头信息，则会添加Accept-Encoding:gzip 头信息
        boolean transparentGzip = false;
        if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
            transparentGzip = true;
            requestBuilder.header("Accept-Encoding", "gzip");
        }

        // 5、获取Request携带的Cookie，如果有Cookie，那么就为 Request添加Cookie到请求头中
        List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
        if (!cookies.isEmpty()) {
            requestBuilder.header("Cookie", cookieHeader(cookies));
        }

        // 6、添加存储客户端的一些信息的信息头：User-Agent
        if (userRequest.header("User-Agent") == null) {
            requestBuilder.header("User-Agent", Version.userAgent());
        }


         /*---------- 这里开始处理Response ----------*/

        // 6、把构建好的Request分发给下一个拦截器，然后获取返回的Response
        Response networkResponse = chain.proceed(requestBuilder.build());

        HttpHeaders.receiveHeaders(cookieJar, userRequest.url(), networkResponse.headers());

        Response.Builder responseBuilder = networkResponse.newBuilder()
                .request(userRequest);

        // 如果在请求头中使用了gzip压缩，那么Response就使用gzip进行解压
        if (transparentGzip
                && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))
                && HttpHeaders.hasBody(networkResponse)) {
            GzipSource responseBody = new GzipSource(networkResponse.body().source());
            Headers strippedHeaders = networkResponse.headers().newBuilder()
                    .removeAll("Content-Encoding")
                    .removeAll("Content-Length")
                    .build();
            responseBuilder.headers(strippedHeaders);
            String contentType = networkResponse.header("Content-Type");
            responseBuilder.body(new RealResponseBody(contentType, -1L, Okio.buffer(responseBody)));
        }

        // 返回处理好的Response
        return responseBuilder.build();
    }

    /**
     * 这里将列表中的Cookie转换成HTTP中规范的Cookie格式
     */
    private String cookieHeader(List<Cookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; i++) {
            if (i > 0) {
                // Cookie之间是以 ";" 进行分隔的
                cookieHeader.append("; ");
            }
            Cookie cookie = cookies.get(i);
            cookieHeader.append(cookie.name()).append('=').append(cookie.value());
        }
        return cookieHeader.toString();
    }
}
