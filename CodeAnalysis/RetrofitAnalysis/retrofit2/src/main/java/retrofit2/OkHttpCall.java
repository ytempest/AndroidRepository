/*
 * Copyright (C) 2015 Square, Inc.
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
package retrofit2;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

import static retrofit2.Utils.checkNotNull;
import static retrofit2.Utils.throwIfFatal;

/**
 * Description：OkHttpCall其实就是 Call的封装类，是一个能进行HTTP网络请求的类，里面封装了自定义
 * 请求接口中的方法，也就是说一个 OkHttpCall会对应一个 url请求
 *
 * @param <T> 这个泛型是在创建OkHttpCall的时候传递过来的，是一个Object类型，但是这个泛型应该代表
 *            后台返回数据所对应的 Result Bean
 */
final class OkHttpCall<T> implements Call<T> {
    private final ServiceMethod<T, ?> serviceMethod;
    // url中键值对的值
    private final @Nullable
    Object[] args;

    private volatile boolean canceled;

    @GuardedBy("this")
    private @Nullable
    okhttp3.Call rawCall;
    @GuardedBy("this") // Either a RuntimeException, non-fatal Error, or IOException.
    private @Nullable
    Throwable creationFailure;
    @GuardedBy("this")
    // 标记这个 OkHttpCall的执行情况，一个OkHttpCall只能执行一次
    private boolean executed;

    OkHttpCall(ServiceMethod<T, ?> serviceMethod, @Nullable Object[] args) {
        this.serviceMethod = serviceMethod;
        this.args = args;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    // We are a final type & this saves clearing state.
    @Override
    public OkHttpCall<T> clone() {
        return new OkHttpCall<>(serviceMethod, args);
    }

    @Override
    public synchronized Request request() {
        okhttp3.Call call = rawCall;
        if (call != null) {
            return call.request();
        }
        if (creationFailure != null) {
            if (creationFailure instanceof IOException) {
                throw new RuntimeException("Unable to create request.", creationFailure);
            } else if (creationFailure instanceof RuntimeException) {
                throw (RuntimeException) creationFailure;
            } else {
                throw (Error) creationFailure;
            }
        }
        try {
            return (rawCall = createRawCall()).request();
        } catch (RuntimeException | Error e) {
            throwIfFatal(e); // Do not assign a fatal error to creationFailure.
            creationFailure = e;
            throw e;
        } catch (IOException e) {
            creationFailure = e;
            throw new RuntimeException("Unable to create request.", e);
        }
    }

    /**
     * 使用异步的方式向后台请求数据
     */
    @Override
    public void enqueue(final Callback<T> callback) {
        // 1、检查回调接口是否为空
        checkNotNull(callback, "callback == null");

        okhttp3.Call call;
        Throwable failure;

        synchronized (this) {
            // 2、检测是否已经执行，防止多次使用Call进行网络请求
            // 表明一个 Call只能执行一次
            if (executed) {
                throw new IllegalStateException("Already executed.");
            }
            executed = true;

            call = rawCall;
            failure = creationFailure;

            // 第一次进来 rawCall 和 creationFailure 肯定都为null
            if (call == null && failure == null) {
                try {
                    // 3、使用 OkHttpCall保存的 serviceMethod和 args构建一个 Call对象
                    call = rawCall = createRawCall();
                } catch (Throwable t) {
                    // 如果在解析apply接口方法中的参数的时候出错就会走到这里
                    throwIfFatal(t);
                    failure = creationFailure = t;
                }
            }
        }

        // 4、如果如果在解析apply接口方法中的参数的时候出错，那么 failure就不为空
        if (failure != null) {
            callback.onFailure(this, failure);
            return;
        }

        if (canceled) {
            call.cancel();
        }

        // 5、这里开始进行网络请求！！！使用 OkHttp进行异步网络请求
        call.enqueue(new okhttp3.Callback() {
            /**
             * 开始处理请求数据成功后的结果
             *
             * @param rawResponse 未进行处理的Response
             */
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) {
                // 注意注意！！！这里就将 Result Bean的类型传入了 Response中
                Response<T> response;
                try {
                    // 6、对使用 OkHttp请求数据后的 Response（响应）进行解析，然后返回一个
                    // Retrofit的 Response
                    response = parseResponse(rawResponse);
                } catch (Throwable e) {
                    throwIfFatal(e);
                    callFailure(e);
                    return;
                }

                try {
                    // 回调给用户
                    callback.onResponse(OkHttpCall.this, response);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callFailure(e);
            }

            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(OkHttpCall.this, e);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public Response<T> execute() throws IOException {
        okhttp3.Call call;

        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("Already executed.");
            }
            executed = true;

            if (creationFailure != null) {
                if (creationFailure instanceof IOException) {
                    throw (IOException) creationFailure;
                } else if (creationFailure instanceof RuntimeException) {
                    throw (RuntimeException) creationFailure;
                } else {
                    throw (Error) creationFailure;
                }
            }

            call = rawCall;
            if (call == null) {
                try {
                    call = rawCall = createRawCall();
                } catch (IOException | RuntimeException | Error e) {
                    throwIfFatal(e); //  Do not assign a fatal error to creationFailure.
                    creationFailure = e;
                    throw e;
                }
            }
        }

        if (canceled) {
            call.cancel();
        }

        return parseResponse(call.execute());
    }

    /**
     * 将 serviceMethod转换成一个OkHttp的 Call对象，然后用于进行网络请求
     */
    private okhttp3.Call createRawCall() throws IOException {
        // 把转换逻辑交给 serviceMethod自身实现，这样就可以实现对serviceMethod中一些数据的隐藏
        // 将 ServiceMethod 转换成一个 OkHttp的 Call
        okhttp3.Call call = serviceMethod.toCall(args);
        if (call == null) {
            throw new NullPointerException("Call.Factory returned null.");
        }
        return call;
    }

    /**
     * 解析 rawResponse，根据状态码对OkHttp的Response进行处理，构建一个新的 Retrofit的 Response
     * 然后返回
     *
     * @param rawResponse 使用OkHttp请求数据后返回的 Response
     */
    Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
        // 1、先从最初的 Response中获取最初的 ResponseBody响应体
        ResponseBody rawBody = rawResponse.body();

        // 2、将最初的 OkHttp.Response 重新构建，除了将其中的 ResponseBody使用 NoContentResponseBody
        // 对象进行替代，其他的数据都没有进行改变
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();

        int code = rawResponse.code();

        // 3、如果该 Response 请求数据失败后返回的 Response
        if (code < 200 || code >= 300) {
            try {
                // 使用 Buffer提前对 ResponseBody进行处理，防止在后面或者用户使用时出现 I/O异常
                // 使用 buffer()方法会将服务器的输出流中的数据写到 Okio中的一个 Buffer中，然后使
                // 用这个 Buffer 重新构建一个 ResponseBody
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                // 返回一个请求失败的 Response
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        // 4、判断状态码是不是 204 或 205；因为两个状态码表示服务器不会返回数据，所以要分开处理
        if (code == 204 || code == 205) {
            // close ResponseBody，防止出错
            rawBody.close();
            // 返回一个请求成功后，但是没有数据的Response
            return Response.success(null, rawResponse);
        }


        // 5、处理 OkHttp请求数据成功后的 Response，将这个Response构建成 Retrofit的 Response，然后返回
        // 5.1、创建一个 ExceptionCatchingRequestBody类来捕获服务器的输入流时可能会出现的异常
        ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
        try {
            // 5.2、调用 ResponseConverter转换器，将后台返回的ResponseBody转换成相应的 Result Bean
            T body = serviceMethod.toResponse(catchingBody);
            // 5.3、返回请求成功后的 Retrofit 的 Response
            return Response.success(body, rawResponse);
        } catch (RuntimeException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    @Override
    public void cancel() {
        canceled = true;

        okhttp3.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    /**
     * Description：这个一个空数据的 ResponseBody对象，保存了有数据的 MediaType和 contentLength，
     * 但是没有数据，一旦调用 source()读取数据就会报异常；
     * 为什么要定义这样一个类？
     * 在 OkHttpCall中的 parseResponse()方法中对 OkHttp的Response 进行转换成 Retrofit的 Response时会用到
     */
    static final class NoContentResponseBody extends ResponseBody {
        private final MediaType contentType;
        private final long contentLength;

        NoContentResponseBody(MediaType contentType, long contentLength) {
            this.contentType = contentType;
            this.contentLength = contentLength;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }
    }

    /**
     * Description：这个类使用了委派模式，ExceptionCatchingRequestBody是一个中介，会把执行的逻辑
     * 都交给委派对象 delegate进行处理；
     * 在 source()方法中会使用一个 ForwardingSource代理 delegate.source()返回的服务器的输入流
     * 在这个代理中 read()方法中捕获读取服务器输入流时可能会出现的异常
     */
    static final class ExceptionCatchingRequestBody extends ResponseBody {
        private final ResponseBody delegate;
        IOException thrownException;

        ExceptionCatchingRequestBody(ResponseBody delegate) {
            this.delegate = delegate;
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        @Override
        public BufferedSource source() {
            return Okio.buffer(new ForwardingSource(delegate.source()) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        @Override
        public void close() {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }
}
