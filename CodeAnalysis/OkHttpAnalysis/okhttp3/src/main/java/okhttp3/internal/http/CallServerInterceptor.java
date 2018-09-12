/*
 * Copyright (C) 2016 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3.internal.http;

import java.io.IOException;
import java.net.ProtocolException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.http1.Http1Codec;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * This is the last interceptor in the chain. It makes a network call to the server.
 * Description：这是责任链中的最后一个拦截器，它负责使用网络与服务器进行交互，从服务器的输入流中获取
 * Response数据，以及将Request中的数据（如：RequestBody）写到服务器的输出流中
 */
public final class CallServerInterceptor implements Interceptor {
    private final boolean forWebSocket;

    public CallServerInterceptor(boolean forWebSocket) {
        this.forWebSocket = forWebSocket;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        // 获取 ConnectInterceptor拦截器创建的HttpCodec
        HttpCodec httpCodec = realChain.httpStream();
        // 获取 RetryAndFollowUpInterceptor拦截器创建的StreamAllocation
        StreamAllocation streamAllocation = realChain.streamAllocation();
        // 获取 ConnectInterceptor拦截器创建的RealConnection
        RealConnection connection = (RealConnection) realChain.connection();

        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();

        // 回调Call事件监听方法，告知开始将请求的头信息写到服务器的输出流中
        realChain.eventListener().requestHeadersStart(realChain.call());

        // 1、这里会将请求的状态行、请求头的头信息写到 服务器的输出流中
        httpCodec.writeRequestHeaders(request);

        // 回调Call事件监听方法
        realChain.eventListener().requestHeadersEnd(realChain.call(), request);

        Response.Builder responseBuilder = null;
        // 判断该请求是否存在RequestBody，以及RequestBody是否不为空（只有GET和HEAD请求方式不存在请求体）
        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
            // If there's a "Expect: 100-continue" header on the request, wait for a "HTTP/1.1 100
            // Continue" response before transmitting the request body. If we don't get that, return
            // what we did get (such as a 4xx response) without ever transmitting the request body.
            // 1、如果请求头上有"Expect: 100-continue"头信息，则在发送RequestBody前等待"HTTP/1.1 100  Continue"
            // 响应，如果没有得到该响应则返回该响应（如：4xx的响应），而不去发送RequestBody
            // PS【http 100-continue用于客户端在发送POST数据给服务器前，征询服务器情况，看服务器是否处
            // 理POST的数据，如果不处理，客户端则不上传POST数据，如果处理，则POST上传数据。在现实应用中，
            // 通过在POST大数据时，才会使用100-continue协议。】
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
                // （1.1）刷新服务器的输出流
                httpCodec.flushRequest();

                // （1.2）回调Call事件监听，告知开始读取服务器的响应头信息
                realChain.eventListener().responseHeadersStart(realChain.call());

                // （1.3）如果使用的Http1.0进行连接，那么HttpCodec接口的实现类是Http1Codec
                // 在这里，readResponseHeaders(true)只会用来获取与"100-continue"相关信息的Response.Builder
                responseBuilder = httpCodec.readResponseHeaders(true);
            }

            // 2、如果请求头信息不包含"Expect:100-continue" 或者包含了但是响应码为100，那么responseBuilder会为空
            if (responseBuilder == null) {
                // Write the request body if the "Expect: 100-continue" expectation was met.

                // （2.1）回调Call事件监听，表示开始将RequestBody写入服务器的输出流
                realChain.eventListener().requestBodyStart(realChain.call());

                long contentLength = request.body().contentLength();
                // （2.2）根据请求体的长度是否可知获取 ChunkedSink或FixedLengthSink，然后使用 CountingSink封装起来
                // CountingSink通过代理的方式实现计算写入到输出流的字节数
                CountingSink requestBodyOut =
                        new CountingSink(httpCodec.createRequestBody(request, contentLength));
                // （2.3）封装服务器的输出流
                BufferedSink bufferedRequestBody = Okio.buffer(requestBodyOut);

                // （2.4）把服务器返回的输出流传递给RequestBody，让其可以向服务器写东西
                // 这里会将RequestBody的内容（即表单数据，如文件，图片，MP3等）写到服务器的输出流中
                request.body().writeTo(bufferedRequestBody);

                // （2.5）关闭服务器的输出流
                bufferedRequestBody.close();

                // （2.6）回调Call事件监听，表示完成将RequestBody写入服务器的输出流
                realChain.eventListener()
                        .requestBodyEnd(realChain.call(), requestBodyOut.successfulCount);

            } else if (!connection.isMultiplexed()) {
                // If the "Expect: 100-continue" expectation wasn't met, prevent the HTTP/1 connection
                // from being reused. Otherwise we're still obligated to transmit the request body to
                // leave the connection in a consistent state.
                streamAllocation.noNewStreams();
            }
        }

        // 3、结束请求，这个方法会刷新服务器的输出流
        httpCodec.finishRequest();

        // 如果没有满足"Expect:100-continue"这个条件，那么这个responseBuilder依旧为空
        if (responseBuilder == null) {
            // 回调Call事件监听
            realChain.eventListener().responseHeadersStart(realChain.call());

            // 4、读取服务器返回的响应头的头信息到 Response中
            // 如果使用的Http1.0进行连接，那么HttpCodec接口的实现类是Http1Codec
            // 从服务器的输入流中读取响应头信息并使用这些信息构建 Response.Builder
            /**{@link Http1Codec#readResponseHeaders(boolean)}*/
            responseBuilder = httpCodec.readResponseHeaders(false);
        }

        // 5、封装Response的相关数据
        Response response = responseBuilder
                .request(request)
                // 将三次握手相关的信息写入到 Response中
                .handshake(streamAllocation.connection().handshake())
                // 设置开始将请求的RequestBody写到服务器的输出流的时间戳
                .sentRequestAtMillis(sentRequestMillis)
                // 设置接收到服务器响应的时间戳
                .receivedResponseAtMillis(System.currentTimeMillis())
                .build();

        int code = response.code();
        if (code == 100) {
            // server sent a 100-continue even though we did not request one.
            // try again to read the actual response
            responseBuilder = httpCodec.readResponseHeaders(false);

            response = responseBuilder
                    .request(request)
                    .handshake(streamAllocation.connection().handshake())
                    .sentRequestAtMillis(sentRequestMillis)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();

            code = response.code();
        }

        // 回调Call事件监听，告知接收到服务器返回的响应头
        // 现在这个Response还没有数据，只有一些响应头信息以及一些额外的参数
        realChain.eventListener()
                .responseHeadersEnd(realChain.call(), response);


        if (forWebSocket && code == 101) {
            // Connection is upgrading, but we need to ensure interceptors see a non-null response body.
            response = response.newBuilder()
                    .body(Util.EMPTY_RESPONSE)
                    .build();
        } else {
            // 6、一般都会走到这里，在这里获取服务器返回的数据
            response = response.newBuilder()
                    // 根据response中的头信息构建一个 RealResponseBody对象，然后保存到response的body中
                    .body(httpCodec.openResponseBody(response))
                    .build();
        }

        if ("close".equalsIgnoreCase(response.request().header("Connection"))
                || "close".equalsIgnoreCase(response.header("Connection"))) {
            streamAllocation.noNewStreams();
        }

        if ((code == 204 || code == 205) && response.body().contentLength() > 0) {
            throw new ProtocolException(
                    "HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
        }

        return response;
    }

    /**
     * Description：这个输出流通过代理的方式代理一个ChunkedSink或FixedLengthSink，然后完成计算Sink的写入到输出流的字节数
     */
    static final class CountingSink extends ForwardingSink {
        long successfulCount;

        CountingSink(Sink delegate) {
            super(delegate);
        }

        /**
         * 如果请求体的长度可知，那么CountingSink代理的是 Http1Codec.ChunkedSink对象；
         * {@link Http1Codec.ChunkedSink#write(Buffer, long)}
         * 如果请求体的长度未知，那么CountingSink代理的是 Http1Codec.FixedLengthSink对象；
         * {@link Http1Codec.FixedLengthSink#write(Buffer, long)}
         *
         * @param source    要写入到服务器输出流的请求体内容，请求体的长度
         * @param byteCount 请求体的长度，准确值或者-1
         */
        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            // 累加请求体内容成功写入服务器的字节数
            successfulCount += byteCount;
        }
    }
}
