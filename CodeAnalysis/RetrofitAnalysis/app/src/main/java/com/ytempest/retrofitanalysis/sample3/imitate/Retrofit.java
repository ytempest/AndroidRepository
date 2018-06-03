package com.ytempest.retrofitanalysis.sample3.imitate;

import android.util.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.ResponseBody;

/**
 * @author ytempest
 *         Description：
 */
public class Retrofit {
    private final static String TAG = "Retrofit";
    private final Map<Method, ServiceMethod<?, ?>> mMethodMethodServiceCache = new ConcurrentHashMap<>();

    private final String mBaseUrl;
    private final Call.Factory mCallFactory;
    private List<Converter.Factory> mConverterFactories;


    public Retrofit(Builder builder, Call.Factory callFactory, List<Converter.Factory> factories) {
        this.mBaseUrl = builder.mBaseUrl;
        this.mCallFactory = callFactory;
        this.mConverterFactories = factories;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> service) {
        // 检测这个是不是接口

        // 创建代理对象
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 如果这个方法是Object的方法，那么直接执醒
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }

                        // 封装 url请求
                        ServiceMethod<Object, Object> serviceMethod =
                                (ServiceMethod<Object, Object>) loadMethodService(method);

                        // 封装成一个 OkHttpCall
                        OkHttpCall okHttpCall = new OkHttpCall(serviceMethod, args);
                        return okHttpCall;
                    }
                });
    }

    private ServiceMethod<?, ?> loadMethodService(Method method) {
        ServiceMethod<?, ?> result = mMethodMethodServiceCache.get(method);
        if (result != null) {
            return result;
        }
        synchronized (mMethodMethodServiceCache) {
            result = mMethodMethodServiceCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method).build();
                mMethodMethodServiceCache.put(method, result);
            }
        }
        return result;
    }

    public <T> Converter<ResponseBody, T> responseBodyConverter(Type responseType, Annotation[] annotations) {
        for (int i = 0; i < mConverterFactories.size(); i++) {
            Converter<ResponseBody, ?> responseBodyConverter = mConverterFactories.get(i)
                    .responseBodyConverter(responseType, annotations, this);
            if (responseBodyConverter != null) {
                //noinspection unchecked
                return (Converter<ResponseBody, T>) responseBodyConverter;
            }
        }
        return null;
    }

    public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
        for (int i = 0; i < mConverterFactories.size(); i++) {
            Converter<?, String> stringConverter = mConverterFactories.get(i)
                    .stringConverter(type, annotations, this);
            if (stringConverter != null) {
                //noinspection unchecked
                return (Converter<T, String>) stringConverter;
            }
        }

        return null;
    }

    public String baseUrl() {
        return mBaseUrl;
    }

    public okhttp3.Call.Factory callFactory() {
        return mCallFactory;
    }



    public static class Builder {
        String mBaseUrl;
        private List<Converter.Factory> mConverterFactories = new ArrayList<>();
        private Call.Factory mCallFactory;

        public Builder baseUrl(String baseUrl) {
            this.mBaseUrl = baseUrl;
            return this;
        }

        public Builder addConverterFactory(Converter.Factory factory) {
            this.mConverterFactories.add(factory);
            return this;
        }

        public Builder client(Call.Factory callFactory) {
            this.mCallFactory = callFactory;
            return this;
        }

        public Retrofit build() {
            // 添加默认的转换器工厂
            mConverterFactories.add(new BuiltInConverterFactory());

            return new Retrofit(this, mCallFactory, Collections.unmodifiableList(mConverterFactories));
        }
    }
}
