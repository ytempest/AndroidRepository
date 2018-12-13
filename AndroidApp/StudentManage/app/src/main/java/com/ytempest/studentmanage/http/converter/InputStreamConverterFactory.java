package com.ytempest.studentmanage.http.converter;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author ytempest
 *         Description：一个生产将ResponseBody转成InputStream的转换器的工厂类
 */
public class InputStreamConverterFactory extends Converter.Factory {

    private InputStreamConverterFactory() {
    }

    public static InputStreamConverterFactory create() {
        return new InputStreamConverterFactory();
    }

    @Override
    public Converter<ResponseBody, InputStream> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        // 判断要转换的类型是否是InputStream，如果不是交个下一个转换器处理
        if (type == InputStream.class) {
            return new InputStreamConverter();
        } else {
            return retrofit.nextResponseBodyConverter(this, type, annotations);
        }
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return retrofit.nextRequestBodyConverter(this, type, parameterAnnotations, methodAnnotations);
    }

}
