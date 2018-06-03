package com.ytempest.retrofitanalysis.sample3.imitate;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;

/**
 * @author ytempest
 *         Description：
 */
public interface Converter<F, T> {
    T convert(F value) throws IOException;

    abstract class Factory {
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                                Retrofit retrofit) {
            return null;
        }

        public Converter<?, String> stringConverter(Type type, Annotation[] annotations,
                                                    Retrofit retrofit) {
            return null;
        }
    }


}
