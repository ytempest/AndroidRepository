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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import okhttp3.Request;

import static retrofit2.Utils.checkNotNull;

/**
 * Description：这是一个CallAdapter工厂类，是 Retrofit在 Android平台默认添加的 CallAdapter工厂类；
 * 用于生产 CallAdapter适配器；但是这个 ExecutorCallAdapterFactory工厂调用 get()生产 CallAdapter
 * 适配器的 adapt()会将原来的Call对象适配成一个 ExecutorCallbackCall（实现了Call接口）对象；这就
 * 意味着，用户使用call.enqueue(new CallBack()) 时，CallBack回调接口中的两个回调方法都会在
 * ExecutorCallbackCall指定的 Executor中执行
 */
final class ExecutorCallAdapterFactory extends CallAdapter.Factory {
    final Executor callbackExecutor;

    ExecutorCallAdapterFactory(Executor callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }
        final Type responseType = Utils.getCallResponseType(returnType);
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return new ExecutorCallbackCall<>(callbackExecutor, call);
            }
        };
    }

    /**
     * Description：这个类实现了 Retrofit的 Call接口，使用委派模式让 ExecutorCallbackCall充当一个
     * 中介，然后将 Call对象原本要处理的逻辑委派给 Call对象进行处理，但是在 ExecutorCallbackCall
     * 中的 enqueue()方法中会调用 Call对象（即委派对象）中的 enqueue() 方法，然后将 Callback回调
     * 接口中的两个回调方法切换到指定的Executor 执行，这个Executor在 ExecutorCallbackCall构造方法
     * 中传入
     *
     * @param <T> CallBack回调接口中的后台返回数据序列化的 Result Bean
     */
    static final class ExecutorCallbackCall<T> implements Call<T> {
        final Executor callbackExecutor;
        // 委派的对象
        final Call<T> delegate;

        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        /**
         * 这个方法会将 CallBack回调接口中的两个回调方法放到指定的 callbackExecutor（线程）中
         * 执行，从而实现切换回调方法执行线程
         *
         * @param callback 委派对象Call对象执行 enqueue()方法的 callback回调接口
         */
        @Override
        public void enqueue(final Callback<T> callback) {
            checkNotNull(callback, "callback == null");

            delegate.enqueue(new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (delegate.isCanceled()) {
                                // Emulate OkHttp's behavior of throwing/delivering an IOException on cancellation.
                                callback.onFailure(ExecutorCallbackCall.this, new IOException("Canceled"));
                            } else {
                                callback.onResponse(ExecutorCallbackCall.this, response);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(ExecutorCallbackCall.this, t);
                        }
                    });
                }
            });
        }

        @Override
        public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override
        public Response<T> execute() throws IOException {
            return delegate.execute();
        }

        @Override
        public void cancel() {
            delegate.cancel();
        }

        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }

        @Override
        public Request request() {
            return delegate.request();
        }
    }
}
