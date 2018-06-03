package com.ytempest.retrofitanalysis.sample3.imitate;

import android.util.Log;

import com.ytempest.retrofitanalysis.sample3.imitate.htpp.Field;
import com.ytempest.retrofitanalysis.sample3.imitate.htpp.POST;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import okhttp3.*;
import okhttp3.Call;


/**
 * @author ytempest
 *         Description：
 */
class ServiceMethod<R, T> {
    private final String mBaseUrl;
    private final String mHttpMethod;
    private final String mRelativeUrl;
    private final ParameterHandler<?>[] mParameterHandlers;
    private final Call.Factory mCallFactory;
    private final Converter<ResponseBody, R> mResponseConverter;

    ServiceMethod(Builder<R, T> builder) {
        this.mBaseUrl = builder.mRetrofit.baseUrl();
        this.mHttpMethod = builder.mHttpMethod;
        this.mRelativeUrl = builder.mRelativeUrl;
        this.mParameterHandlers = builder.mParameterHandlers;
        this.mCallFactory = builder.mRetrofit.callFactory();
        this.mResponseConverter = builder.mResponseConverter;
    }

    public okhttp3.Call toCall(Object[] args) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(mHttpMethod, mBaseUrl, mRelativeUrl);

        @SuppressWarnings("unchecked")
        ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) mParameterHandlers;

        for (int i = 0; i < mParameterHandlers.length; i++) {
            handlers[i].apply(requestBuilder, args[i]);
        }

        return mCallFactory.newCall(requestBuilder.build());
    }

    R toResponse(ResponseBody rawBody) throws IOException {
        return mResponseConverter.convert(rawBody);
    }

    public static class Builder<R, T> {
        final Retrofit mRetrofit;
        final Method mMethod;
        private final Type[] mParameterTypes;
        private final Annotation[][] mParameterAnnotations;
        private Converter<ResponseBody, R> mResponseConverter;
        private Type mResponseType;
        final Annotation[] mMethodAnnotations;
        String mHttpMethod;
        String mRelativeUrl;
        private ParameterHandler<?>[] mParameterHandlers;

        public Builder(Retrofit retrofit, Method method) {
            this.mRetrofit = retrofit;
            this.mMethod = method;
            this.mMethodAnnotations = method.getAnnotations();
            this.mParameterAnnotations = method.getParameterAnnotations();
            this.mParameterTypes = method.getGenericParameterTypes();
        }

        public ServiceMethod<?, ?> build() {
            // 获取接口方法返回值的 Result Bean
            mResponseType = Utils.getParameterUpperBound(0,
                    (ParameterizedType) mMethod.getGenericReturnType());

            // 创建数据转换器
            mResponseConverter = createResponseConverter();

            // 解析接口方法上标记的注解
            for (Annotation methodAnnotation : mMethodAnnotations) {
                parseMethodAnnotation(methodAnnotation);
            }

            // 解析接口方法的参数注解，封装成一个 ParameterHandler对象
            int count = mParameterAnnotations.length;
            mParameterHandlers = new ParameterHandler<?>[count];
            for (int i = 0; i < count; i++) {
                Type parameterType = mParameterTypes[i];
                mParameterHandlers[i] = parseParameter(i, parameterType, mParameterAnnotations[i]);
            }

            // 最后实例化
            return new ServiceMethod<>(this);
        }

        private ParameterHandler<?> parseParameter(int i, Type parameterType, Annotation[] parameterAnnotation) {
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof Field) {
                    Field field = (Field) annotation;
                    String name = field.value();
                    Converter<?, String> stringConverter =
                            mRetrofit.stringConverter(parameterType, parameterAnnotation);
                    return new ParameterHandler.Field<>(name, stringConverter);
                }
            }

            return null;
        }

        private void parseMethodAnnotation(Annotation methodAnnotation) {
            if (methodAnnotation instanceof POST) {
                POST post = (POST) methodAnnotation;
                String value = post.value();
                mHttpMethod = "POST";
                mRelativeUrl = value;
            }
        }

        private Converter<ResponseBody, R> createResponseConverter() {
            return mRetrofit.responseBodyConverter(mResponseType, mMethodAnnotations);
        }


    }
}
