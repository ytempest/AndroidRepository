package com.ytempest.retrofitanalysis.sample3.imitate;

import android.content.pm.PackageManager;

import java.io.IOException;

/**
 * @author ytempest
 *         Description：
 */
public abstract class ParameterHandler<T> {
    abstract void apply(RequestBuilder requestBuilder, T value) throws IOException;


    static final class Field<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> stringConverter;

        public Field(String name, Converter<T, String> stringConverter) {
            this.name = name;
            this.stringConverter = stringConverter;
        }

        @Override
        void apply(RequestBuilder requestBuilder, T value) throws IOException {
            String result = stringConverter.convert(value);
            requestBuilder.addFormField(name, result);
        }
    }
}

