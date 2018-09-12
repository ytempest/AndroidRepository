/*
 * Copyright (C) 2014 Square, Inc.
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
package okhttp3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.NamedRunnable;
import okhttp3.internal.cache.CacheInterceptor;
import okhttp3.internal.connection.ConnectInterceptor;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.http.BridgeInterceptor;
import okhttp3.internal.http.CallServerInterceptor;
import okhttp3.internal.http.RealInterceptorChain;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;
import okhttp3.internal.platform.Platform;

import static okhttp3.internal.platform.Platform.INFO;

final class RealCall implements Call {
    final OkHttpClient client;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;

    /**
     * There is a cycle between the {@link Call} and {@link EventListener} that makes this awkward.
     * This will be set after we create the call instance then create the event listener instance.
     */
    private EventListener eventListener;

    /**
     * The application's original request unadulterated by redirects or auth headers.
     * 这是最初的请求，还没有进行重定向和加入其它头部
     */
    final Request originalRequest;
    final boolean forWebSocket;

    // Guarded by this.
    private boolean executed;

    private RealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client, forWebSocket);
    }

    static RealCall newRealCall(OkHttpClient client, Request originalRequest, boolean forWebSocket) {
        // Safely publish the Call instance to the EventListener.
        RealCall call = new RealCall(client, originalRequest, forWebSocket);
        call.eventListener = client.eventListenerFactory().create(call);
        return call;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    /**
     * 使用同步请求的方式向服务器请求数据，也就是等待服务器返回数据后才会进行下一步逻辑
     */
    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        try {
            client.dispatcher().executed(this);
            // getResponseWithInterceptorChain()方法会构建责任链向服务器发起请求获取响应
            // 这个 Response就是服务器返回的Response（或者是缓存中的Response）
            Response result = getResponseWithInterceptorChain();
            if (result == null) throw new IOException("Canceled");
            return result;
        } catch (IOException e) {
            eventListener.callFailed(this, e);
            throw e;
        } finally {
            client.dispatcher().finished(this);
        }
    }

    private void captureCallStackTrace() {
        Object callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()");
        retryAndFollowUpInterceptor.setCallStackTrace(callStackTrace);
    }

    /**
     * 使用异步的方式请求数据
     * 会创建一个 AsyncCall对象，这个对象封装了请求的相关参数，同时持有RealCall的引用；
     * 然后会将这个对象放到线程池中执行，并通过回调接口获取请求的结果
     *
     * @param responseCallback 回调接口
     */
    @Override
    public void enqueue(Callback responseCallback) {
        // 调用enqueue方法进行请求后会锁定RealCall，也就是说一个RealCall同时只能被一个线程使用、请求
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        // AsyncCall是 RealCall类中的一个内部类，AsyncCall实现了Runnable接口，
        // 所以说这个对象最终会交给线程池去执行
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    /**
     *
     */
    @Override
    public void cancel() {
        retryAndFollowUpInterceptor.cancel();
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return retryAndFollowUpInterceptor.isCanceled();
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    // We are a final type & this saves clearing state.
    @Override
    public RealCall clone() {
        return RealCall.newRealCall(client, originalRequest, forWebSocket);
    }

    StreamAllocation streamAllocation() {
        return retryAndFollowUpInterceptor.streamAllocation();
    }


    /**
     * Description：这个AsyncCall封装了回调接口，同时持有RealCall的引用，当这个对象的execute()执行
     * 时就会开始进行请求数据，同时回调接口的方法
     */
    final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        AsyncCall(Callback responseCallback) {
            super("OkHttp %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        String host() {
            return originalRequest.url().host();
        }

        Request request() {
            return originalRequest;
        }

        RealCall get() {
            return RealCall.this;
        }


        /**
         * 当这个对象被扔到线程池执行时，run() 方法会回调这个方法
         * 处理逻辑：完成向服务器请求数据并获取响应数据，根据用户是否调用 RealCall的 cancel()方法
         * 决定调用请求失败的回调还是调用请求成功的回调
         */
        @Override
        protected void execute() {
            // 标记是否已经调用了回调接口
            boolean signalledCallback = false;
            try {
                // getResponseWithInterceptorChain()方法会构建责任链向服务器发起请求获取响应
                // 这个 Response就是服务器返回的Response（或者是缓存中的Response）
                Response response = getResponseWithInterceptorChain();
                if (retryAndFollowUpInterceptor.isCanceled()) {
                    // 如果用户调用了RealCall的cancel()取消了本次请求，那么就会走这里
                    signalledCallback = true;
                    responseCallback.onFailure(RealCall.this, new IOException("Canceled"));

                } else {
                    // 走到这里表示请求成功，将Response回调给使用者
                    signalledCallback = true;
                    responseCallback.onResponse(RealCall.this, response);
                }
            } catch (IOException e) {
                if (signalledCallback) {
                    // Do not signal the callback twice!
                    Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
                } else {
                    eventListener.callFailed(RealCall.this, e);
                    responseCallback.onFailure(RealCall.this, e);
                }
            } finally {
                client.dispatcher().finished(this);
            }
        }
    }

    /**
     * Returns a string that describes this call. Doesn't include a full URL as that might contain
     * sensitive information.
     */
    String toLoggableString() {
        return (isCanceled() ? "canceled " : "")
                + (forWebSocket ? "web socket" : "call")
                + " to " + redactedUrl();
    }

    String redactedUrl() {
        return originalRequest.url().redact();
    }

    /**
     * 这个方法用于构建一个请求的责任链，然后发起请求获取后台请求的数据，将这个请求沿着责任链不断传递
     * 下去，等责任链传递到达末尾，会由CallServerInterceptor拦截器与服务器进行交互获取数据，返回Response
     *
     * @return 返回服务器的响应数据Response，这个Response是从CallServerInterceptor拦截器不断往回传递
     * 最终传递到这里的
     */
    Response getResponseWithInterceptorChain() throws IOException {
        // 1、建立一个网络请求拦截器的责任链
        List<Interceptor> interceptors = new ArrayList<>();

        // 2、添加用户自定义的拦截器
        interceptors.addAll(client.interceptors());

        // 3、添加OkHttp自身的拦截器
        interceptors.add(retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(client.cookieJar()));

        // client.internalCache()获取的就是用户定义的缓存Cache对象
        interceptors.add(new CacheInterceptor(client.internalCache()));

        // 添加连接网络的拦截器，这个拦截器过后就表示已经连接网络了
        interceptors.add(new ConnectInterceptor(client));

        if (!forWebSocket) {
            // 添加用户设置的网络拦截器
            interceptors.addAll(client.networkInterceptors());
        }
        // 添加向服务器发起请求的拦截器（文件上传，表单提交都在这个拦截器里面）
        interceptors.add(new CallServerInterceptor(forWebSocket));

        // 4、创建责任链的头部，并传入所有的处理者对象（即所有的Interceptor），并指定第一个处理者对象
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
                originalRequest, this, eventListener, client.connectTimeoutMillis()/*连接超时*/,
                client.readTimeoutMillis()/*读取超时*/, client.writeTimeoutMillis()/*写入超时*/);

        // 5、开始在责任链中传递责任
        return chain.proceed(originalRequest);
    }
}
