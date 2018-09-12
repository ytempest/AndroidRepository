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
import java.io.InterruptedIOException;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Address;
import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.EventListener;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.connection.RouteException;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.http2.ConnectionShutdownException;

import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_PROXY_AUTH;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static okhttp3.internal.Util.closeQuietly;
import static okhttp3.internal.http.StatusLine.HTTP_PERM_REDIRECT;
import static okhttp3.internal.http.StatusLine.HTTP_TEMP_REDIRECT;

/**
 * Description：这个拦截器是一个处理重试和重定向的拦截器
 */
public final class RetryAndFollowUpInterceptor implements Interceptor {
    /**
     * 这个表示重定向的次数
     */
    private static final int MAX_FOLLOW_UPS = 20;

    private final OkHttpClient client;
    private final boolean forWebSocket;
    private volatile StreamAllocation streamAllocation;
    private Object callStackTrace;
    private volatile boolean canceled;

    public RetryAndFollowUpInterceptor(OkHttpClient client, boolean forWebSocket) {
        this.client = client;
        this.forWebSocket = forWebSocket;
    }

    /**
     * Immediately closes the socket connection if it's currently held. Use this to interrupt an
     * in-flight request from any thread. It's the caller's responsibility to close the request body
     * and response body streams; otherwise resources may be leaked.
     * <p>
     * <p>This method is safe to be called concurrently, but provides limited guarantees. If a
     * transport layer connection has been established (such as a HTTP/2 stream) that is terminated.
     * Otherwise if a socket connection is being established, that is terminated.
     */
    public void cancel() {
        canceled = true;
        StreamAllocation streamAllocation = this.streamAllocation;
        if (streamAllocation != null) streamAllocation.cancel();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCallStackTrace(Object callStackTrace) {
        this.callStackTrace = callStackTrace;
    }

    public StreamAllocation streamAllocation() {
        return streamAllocation;
    }

    /**
     * 由拦截器的拦截顺序可以知道，这个拦截器是第一个接触到Request的系统拦截器（用户设置的一个
     * 拦截会首先接触到Request），在这个方法会处理重试和重定向
     *
     * @param chain 下一个拦截器的 chain对象，即 BridgeInterceptor拦截器
     * @return 返回一个Response数据
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {

        /*---------- 这里开始处理Request ----------*/

        // 1、先获取Request对象
        Request request = chain.request();

        // 2、获取相关的参数
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Call call = realChain.call();
        EventListener eventListener = realChain.eventListener();

        // 初始化 StreamAllocation，这个对象在责任链传递过程中非常重要
        // 但是记得了干什么的了
        // TODO: 2018/5/11/011
        StreamAllocation streamAllocation = new StreamAllocation(client.connectionPool(),
                createAddress(request.url())/*将Request的一些信息封装成一个Address*/, call, eventListener, callStackTrace);
        this.streamAllocation = streamAllocation;

        int followUpCount = 0;
        Response priorResponse = null;
        // 这是一个死循环，只有两种情况可以跳出：
        // （1）return Response：也就是请求结束，返回数据
        // （2）throws IOException：也就是出现异常，比如：用户取消了请求、重定向的次数超过20次
        while (true) {
            // 如果用户调用 RealCall的cancel()取消了请求，那么canceled为true
            if (canceled) {
                streamAllocation.release();
                throw new IOException("Canceled");
            }

            Response response;
            // 表示释放连接，如果没有发生异常，那么就不会释放
            boolean releaseConnection = true;
            try {
                // RetryAndFollowUpInterceptor已经处理完了对Request的逻辑，现在把责任下发到下一个拦截器
                // 获取到下一个拦截器返回的 Response后，会对这个Response进行一些处理
                response = realChain.proceed(request, streamAllocation, null, null);
                releaseConnection = false;
            } catch (RouteException e) {
                if (!recover(e.getLastConnectException(), streamAllocation, false, request)) {
                    throw e.getLastConnectException();
                }
                releaseConnection = false;
                continue;
            } catch (IOException e) {
                // 判断这个异常是不是致命，如果是致命的，那么就不会重新发送 Request，否则会重试
                boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
                if (!recover(e, streamAllocation, requestSendStarted, request)) throw e;

                // 设置释放连接为 false，表示别释放连接，我还要重试
                releaseConnection = false;
                // 结束本次循环，重新进行发送Request
                continue;
            } finally {
                // We're throwing an unchecked exception. Release any resources.
                if (releaseConnection) {
                    streamAllocation.streamFailed(null);
                    streamAllocation.release();
                }
            }


            /*---------- 这里开始处理Response ----------*/
            // 如果执行到这里表示，请求成功，这过程中并没有发生异常，或者说异常已经被处理
            // 下面开始处理后面拦截器返回的 Response

            // 第一次循环肯定是null，第二次循环后不会null
            if (priorResponse != null) {
                response = response.newBuilder()
                        .priorResponse(priorResponse.newBuilder()
                                .body(null)
                                .build())
                        .build();
            }

            // 这是重定向的 Request
            Request followUp;
            try {
                // 获取重定向之后的 Request，如果重定向成功，即followUp不为空
                followUp = followUpRequest(response, streamAllocation.route());
            } catch (IOException e) {
                streamAllocation.release();
                throw e;
            }

            // 如果followUp为空，表示没有发生重定向，或者说已经在上一次循环中完成了重定向（如果是，
            // 那么使用 followUpRequest() 方法获取重定向之后的Request时，会返回null）
            if (followUp == null) {
                if (!forWebSocket) {
                    streamAllocation.release();
                }
                // 由于完成了所有重定向，所以会跳出死循环，把Response返回给上一级
                return response;
            }


            /*------- 走到这里表示重定向失败 --------*/
            closeQuietly(response.body());

            // 如果重定向的次数超过20次，那么就直接抛异常
            if (++followUpCount > MAX_FOLLOW_UPS) {
                streamAllocation.release();
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }

            if (followUp.body() instanceof UnrepeatableRequestBody) {
                streamAllocation.release();
                throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
            }

            // 这里还会判断重定向后的url是否和之前的一样，如果不一样
            if (!sameConnection(response, followUp.url())) {
                streamAllocation.release();
                streamAllocation = new StreamAllocation(client.connectionPool(),
                        createAddress(followUp.url()), call, eventListener, callStackTrace);
                this.streamAllocation = streamAllocation;
            } else if (streamAllocation.codec() != null) {
                throw new IllegalStateException("Closing the body of " + response
                        + " didn't close its backing stream. Bad interceptor?");
            }

            // 把当前的Request指向重定向后的request
            request = followUp;
            // 来到这里表示第一次循环没有请求成功，将priorResponse指向下一个拦截器返回的Response
            priorResponse = response;
        }
    }

    /**
     * 将 Request的一些信息（主机号、端口号、DNS、主机名、证书、代理、协议等）封装成一个 Address
     * 如果该url是一个Https请求，那么还会将SSL相关的参数封装到Address中
     *
     * @param url 请求的HttpUrl
     */
    private Address createAddress(HttpUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        // 判断是否是Https请求，如果是就添加SSL相关的参数
        if (url.isHttps()) {
            sslSocketFactory = client.sslSocketFactory();
            hostnameVerifier = client.hostnameVerifier();
            certificatePinner = client.certificatePinner();
        }

        return new Address(url.host(), url.port(), client.dns(), client.socketFactory(),
                sslSocketFactory, hostnameVerifier, certificatePinner, client.proxyAuthenticator(),
                client.proxy(), client.protocols(), client.connectionSpecs(), client.proxySelector());
    }

    /**
     * 该方法会对IOException进行处理，判断该异常是不是致命的，如果不是便返回一个true，告诉调用者，可以
     * 尝试恢复连接（即重试）
     */
    private boolean recover(IOException e, StreamAllocation streamAllocation,
                            boolean requestSendStarted, Request userRequest) {
        streamAllocation.streamFailed(e);

        // The application layer has forbidden retries.
        if (!client.retryOnConnectionFailure()) return false;

        // We can't send the request body again.
        if (requestSendStarted && userRequest.body() instanceof UnrepeatableRequestBody)
            return false;

        // 如果这个异常是致命的
        if (!isRecoverable(e, requestSendStarted)) return false;

        // No more routes to attempt.
        if (!streamAllocation.hasMoreRoutes()) return false;

        // For failure recovery, use the same route selector with a new connection.
        return true;
    }

    private boolean isRecoverable(IOException e, boolean requestSendStarted) {
        // If there was a protocol problem, don't recover.
        if (e instanceof ProtocolException) {
            return false;
        }

        // If there was an interruption don't recover, but if there was a timeout connecting to a route
        // we should try the next route (if there is one).
        if (e instanceof InterruptedIOException) {
            return e instanceof SocketTimeoutException && !requestSendStarted;
        }

        // Look for known client-side or negotiation errors that are unlikely to be fixed by trying
        // again with a different route.
        if (e instanceof SSLHandshakeException) {
            // If the problem was a CertificateException from the X509TrustManager,
            // do not retry.
            if (e.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (e instanceof SSLPeerUnverifiedException) {
            // e.g. a certificate pinning error.
            return false;
        }

        // An example of one we might want to retry with a different route is a problem connecting to a
        // proxy and would manifest as a standard IOException. Unless it is one we know we should not
        // retry, we return true and try a new route.
        return true;
    }

    /**
     * 在这个方法中会根据返回的response进行处理重定向（如果有必要）
     *
     * @param userResponse 下一个拦截器返回的response
     * @return 返回一个重定向之后的Request，里面保存了重定向后的url
     */
    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        if (userResponse == null) throw new IllegalStateException();
        // 获取响应码
        int responseCode = userResponse.code();

        // 获取请求的类型（post、get）
        final String method = userResponse.request().method();
        // 根据响应码处理相应的逻辑，如重定向
        switch (responseCode) {
            case HTTP_PROXY_AUTH: /* 407状态码 */
                Proxy selectedProxy = route != null
                        ? route.proxy()
                        : client.proxy();
                if (selectedProxy.type() != Proxy.Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                return client.proxyAuthenticator().authenticate(route, userResponse);

            case HTTP_UNAUTHORIZED: /* 401状态码 */
                return client.authenticator().authenticate(route, userResponse);

            case HTTP_PERM_REDIRECT: /* 308状态码：重定向状态码 */
            case HTTP_TEMP_REDIRECT: /* 307状态码：重定向状态码 */
                // "If the 307 or 308 status code is received in response to a request other than GET
                // or HEAD, the user agent MUST NOT automatically redirect the request"
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    return null;
                }
                // fall-through
            case HTTP_MULT_CHOICE: /* 300状态码 */
            case HTTP_MOVED_PERM: /* 301状态码 */
            case HTTP_MOVED_TEMP: /* 302状态码 */
            case HTTP_SEE_OTHER: /* 303状态码 */
                // 当这个状态码是303，表示由于请求对应的资源存在着另一个URI，应使用GET方法定向获取请求的资源

                if (!client.followRedirects()) return null;

                // 从头部信息里面获取 Location 新的链接
                String location = userResponse.header("Location");
                if (location == null) return null;
                // 将 Location的信息中提取 url
                HttpUrl url = userResponse.request().url().resolve(location);

                // Don't follow redirects to unsupported protocols.
                if (url == null) return null;

                // 下面的逻辑就是去生成一个新的url
                boolean sameScheme = url.scheme().equals(userResponse.request().url().scheme());
                if (!sameScheme && !client.followSslRedirects()) return null;

                // Most redirects don't include a request body.
                Request.Builder requestBuilder = userResponse.request().newBuilder();
                if (HttpMethod.permitsRequestBody(method)) {
                    final boolean maintainBody = HttpMethod.redirectsWithBody(method);
                    if (HttpMethod.redirectsToGet(method)) {
                        requestBuilder.method("GET", null);
                    } else {
                        RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
                        requestBuilder.method(method, requestBody);
                    }
                    if (!maintainBody) {
                        requestBuilder.removeHeader("Transfer-Encoding");
                        requestBuilder.removeHeader("Content-Length");
                        requestBuilder.removeHeader("Content-Type");
                    }
                }

                // When redirecting across hosts, drop all authentication headers. This
                // is potentially annoying to the application layer since they have no
                // way to retain them.
                if (!sameConnection(userResponse, url)) {
                    requestBuilder.removeHeader("Authorization");
                }

                // 返回一个重定向的 url
                return requestBuilder.url(url).build();

            case HTTP_CLIENT_TIMEOUT: /* 408状态码：请求超时 */
                // 408's are rare in practice, but some servers like HAProxy use this response code. The
                // spec says that we may repeat the request without modifications. Modern browsers also
                // repeat the request (even non-idempotent ones.)
                if (!client.retryOnConnectionFailure()) {
                    // The application layer has directed us not to retry the request.
                    return null;
                }

                if (userResponse.request().body() instanceof UnrepeatableRequestBody) {
                    return null;
                }

                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == HTTP_CLIENT_TIMEOUT) {
                    // We attempted to retry and got another timeout. Give up.
                    return null;
                }

                if (retryAfter(userResponse, 0) > 0) {
                    return null;
                }

                return userResponse.request();

            case HTTP_UNAVAILABLE:  /* 503状态码：服务器不可用 */
                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == HTTP_UNAVAILABLE) {
                    // We attempted to retry and got another timeout. Give up.
                    return null;
                }

                if (retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
                    // specifically received an instruction to retry without delay
                    return userResponse.request();
                }

                return null;

            default:
                return null;
        }
    }

    private int retryAfter(Response userResponse, int defaultDelay) {
        String header = userResponse.header("Retry-After");

        if (header == null) {
            return defaultDelay;
        }

        // https://tools.ietf.org/html/rfc7231#section-7.1.3
        // currently ignores a HTTP-date, and assumes any non int 0 is a delay
        if (header.matches("\\d+")) {
            return Integer.valueOf(header);
        }

        return Integer.MAX_VALUE;
    }

    /**
     * Returns true if an HTTP request for {@code followUp} can reuse the connection used by this
     * engine.
     */
    private boolean sameConnection(Response response, HttpUrl followUp) {
        HttpUrl url = response.request().url();
        return url.host().equals(followUp.host())
                && url.port() == followUp.port()
                && url.scheme().equals(followUp.scheme());
    }
}
