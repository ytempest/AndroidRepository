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
import java.lang.reflect.Array;
import java.util.Map;

import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static retrofit2.Utils.checkNotNull;

/**
 * Description：这是一个参数处理类，能对api接口方法中的参数进行处理，这种处理是指将这个参数按照
 * 其所标注的注解，将这个参数解析成指定的数据，然后添加到 RequestBuilder中；
 * 这个类定义了一个抽象 apply()方法，这个方法主要是将 接口方法的参数值value解析后的数据添加
 * 到 RequestBuilder中
 * <p>
 * 因为接口方法的注解类型有很多，导致接口方法参数的类型也有很多，如果要在一个地方对参数进行
 * 解析然后添加到 RequestBuilder中的话，这就会导致代码量过大，而且责任分散；
 * 对这个问题的解决方案就是：为每一个参数类型地定义一个类，让这个类进行将它负责处理的参数绑
 * 定到 RequestBuilder中，这样就解决了责任分散的问题
 *
 * @param <T> 方法参数的类型，这个类型是要转化为该方法参数的注解所要求的类型，即转化成 url中
 *           键值对中键所对应的值（如：上传的文件要对应文件流）
 */
abstract class ParameterHandler<T> {
    abstract void apply(RequestBuilder builder, @Nullable T value) throws IOException;

    final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>() {
            @Override
            void apply(RequestBuilder builder, @Nullable Iterable<T> values)
                    throws IOException {
                if (values == null) return; // Skip null values.

                for (T value : values) {
                    ParameterHandler.this.apply(builder, value);
                }
            }
        };
    }

    final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>() {
            @Override
            void apply(RequestBuilder builder, @Nullable Object values) throws IOException {
                if (values == null) return; // Skip null values.

                for (int i = 0, size = Array.getLength(values); i < size; i++) {
                    //noinspection unchecked
                    ParameterHandler.this.apply(builder, (T) Array.get(values, i));
                }
            }
        };
    }

    static final class RelativeUrl extends ParameterHandler<Object> {
        @Override
        void apply(RequestBuilder builder, @Nullable Object value) {
            checkNotNull(value, "@Url parameter is null.");
            builder.setRelativeUrl(value);
        }
    }

    static final class Header<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;

        Header(String name, Converter<T, String> valueConverter) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.

            String headerValue = valueConverter.convert(value);
            if (headerValue == null) return; // Skip converted but null values.

            builder.addHeader(name, headerValue);
        }
    }

    static final class Path<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        Path(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Path parameter \"" + name + "\" value must not be null.");
            }
            builder.addPathParam(name, valueConverter.convert(value), encoded);
        }
    }

    static final class Query<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        Query(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.

            String queryValue = valueConverter.convert(value);
            if (queryValue == null) return; // Skip converted but null values

            builder.addQueryParam(name, queryValue, encoded);
        }
    }

    static final class QueryName<T> extends ParameterHandler<T> {
        private final Converter<T, String> nameConverter;
        private final boolean encoded;

        QueryName(Converter<T, String> nameConverter, boolean encoded) {
            this.nameConverter = nameConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.
            builder.addQueryParam(nameConverter.convert(value), null, encoded);
        }
    }

    static final class QueryMap<T> extends ParameterHandler<Map<String, T>> {
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        QueryMap(Converter<T, String> valueConverter, boolean encoded) {
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Query map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw new IllegalArgumentException("Query map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw new IllegalArgumentException(
                            "Query map contained null value for key '" + entryKey + "'.");
                }

                String convertedEntryValue = valueConverter.convert(entryValue);
                if (convertedEntryValue == null) {
                    throw new IllegalArgumentException("Query map value '"
                            + entryValue
                            + "' converted to null by "
                            + valueConverter.getClass().getName()
                            + " for key '"
                            + entryKey
                            + "'.");
                }

                builder.addQueryParam(entryKey, convertedEntryValue, encoded);
            }
        }
    }

    static final class HeaderMap<T> extends ParameterHandler<Map<String, T>> {
        private final Converter<T, String> valueConverter;

        HeaderMap(Converter<T, String> valueConverter) {
            this.valueConverter = valueConverter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Header map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String headerName = entry.getKey();
                if (headerName == null) {
                    throw new IllegalArgumentException("Header map contained null key.");
                }
                T headerValue = entry.getValue();
                if (headerValue == null) {
                    throw new IllegalArgumentException(
                            "Header map contained null value for key '" + headerName + "'.");
                }
                builder.addHeader(headerName, valueConverter.convert(headerValue));
            }
        }
    }

    static final class Field<T> extends ParameterHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        Field(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.

            String fieldValue = valueConverter.convert(value);
            if (fieldValue == null) return; // Skip null converted values

            builder.addFormField(name, fieldValue, encoded);
        }
    }

    static final class FieldMap<T> extends ParameterHandler<Map<String, T>> {
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        FieldMap(Converter<T, String> valueConverter, boolean encoded) {
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Field map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw new IllegalArgumentException("Field map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw new IllegalArgumentException(
                            "Field map contained null value for key '" + entryKey + "'.");
                }

                String fieldEntry = valueConverter.convert(entryValue);
                if (fieldEntry == null) {
                    throw new IllegalArgumentException("Field map value '"
                            + entryValue
                            + "' converted to null by "
                            + valueConverter.getClass().getName()
                            + " for key '"
                            + entryKey
                            + "'.");
                }

                builder.addFormField(entryKey, fieldEntry, encoded);
            }
        }
    }

    static final class Part<T> extends ParameterHandler<T> {
        private final Headers headers;
        private final Converter<T, RequestBody> converter;

        Part(Headers headers, Converter<T, RequestBody> converter) {
            this.headers = headers;
            this.converter = converter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) return; // Skip null values.

            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw new RuntimeException("Unable to convert " + value + " to RequestBody", e);
            }
            builder.addPart(headers, body);
        }
    }

    static final class RawPart extends ParameterHandler<MultipartBody.Part> {
        static final RawPart INSTANCE = new RawPart();

        private RawPart() {
        }

        @Override
        void apply(RequestBuilder builder, @Nullable MultipartBody.Part value) {
            if (value != null) { // Skip null values.
                builder.addPart(value);
            }
        }
    }

    static final class PartMap<T> extends ParameterHandler<Map<String, T>> {
        private final Converter<T, RequestBody> valueConverter;
        private final String transferEncoding;

        PartMap(Converter<T, RequestBody> valueConverter, String transferEncoding) {
            this.valueConverter = valueConverter;
            this.transferEncoding = transferEncoding;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw new IllegalArgumentException("Part map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw new IllegalArgumentException("Part map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw new IllegalArgumentException(
                            "Part map contained null value for key '" + entryKey + "'.");
                }

                Headers headers = Headers.of(
                        "Content-Disposition", "form-data; name=\"" + entryKey + "\"",
                        "Content-Transfer-Encoding", transferEncoding);

                builder.addPart(headers, valueConverter.convert(entryValue));
            }
        }
    }

    static final class Body<T> extends ParameterHandler<T> {
        private final Converter<T, RequestBody> converter;

        Body(Converter<T, RequestBody> converter) {
            this.converter = converter;
        }

        @Override
        void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) {
                throw new IllegalArgumentException("Body parameter value must not be null.");
            }
            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw new RuntimeException("Unable to convert " + value + " to RequestBody", e);
            }
            builder.setBody(body);
        }
    }
}
