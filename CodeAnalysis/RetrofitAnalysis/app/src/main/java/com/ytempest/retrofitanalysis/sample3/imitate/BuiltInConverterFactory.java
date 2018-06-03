package com.ytempest.retrofitanalysis.sample3.imitate;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author ytempest
 *         Description：
 */
public class BuiltInConverterFactory extends Converter.Factory {
    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new Converter<Object, String>() {
            @Override
            public String convert(Object value) throws IOException {
                return value.toString();
            }
        };
    }
}
