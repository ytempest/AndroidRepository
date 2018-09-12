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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Nullable;

import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.ByteString;
import okio.Okio;
import okio.Source;

public abstract class RequestBody {
    /**
     * Returns the Content-Type header for this body.
     */
    public abstract @Nullable
    MediaType contentType();

    /**
     * Returns the number of bytes that will be written to {@code sink} in a call to {@link #writeTo},
     * or -1 if that count is unknown.
     */
    public long contentLength() throws IOException {
        return -1;
    }

    /**
     * Writes the content of this request to {@code sink}.
     * 实现将RequestBody的内容写到Sink中，这个Sink其实是一个RealBufferedSink
     * 这个Sink是服务器的输出流，其被包装的顺序为：
     * （1）如果Request的RequestBody长度未知：
     * --> Sink --> RealBufferedSink --> Http1Codec.ChunkedSink --> CountingSink --> RealBufferedSink
     * （2）如果Request的RequestBody长度可知：
     * --> Sink --> RealBufferedSink --> Http1Codec.FixedLengthSink --> CountingSink --> RealBufferedSink
     */
    public abstract void writeTo(BufferedSink sink) throws IOException;

    /**
     * Returns a new request body that transmits {@code content}. If {@code contentType} is non-null
     * and lacks a charset, this will use UTF-8.
     */
    public static RequestBody create(@Nullable MediaType contentType, String content) {
        Charset charset = Util.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = Util.UTF_8;
                contentType = MediaType.parse(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    /**
     * Returns a new request body that transmits {@code content}.
     */
    public static RequestBody create(
            final @Nullable MediaType contentType, final ByteString content) {
        return new RequestBody() {
            @Override
            public @Nullable
            MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() throws IOException {
                return content.size();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    /**
     * Returns a new request body that transmits {@code content}.
     */
    public static RequestBody create(final @Nullable MediaType contentType, final byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    /**
     * Returns a new request body that transmits {@code content}.
     */
    public static RequestBody create(final @Nullable MediaType contentType, final byte[] content,
                                     final int offset, final int byteCount) {
        if (content == null) throw new NullPointerException("content == null");
        Util.checkOffsetAndCount(content.length, offset, byteCount);
        return new RequestBody() {
            @Override
            public @Nullable
            MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return byteCount;
            }

            /**
             * 实现将RequestBody的数据写到Sink中，首先会调用RealBufferedSink的write()方法
             * {@link okio.RealBufferedSink#write(byte[], int, int)}
             *
             * @param sink 这个Sink其实是一个RealBufferedSink
             */
            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }

    /**
     * 把要上传的文件，以及文件的MimeType类型封装成一个 RequestBody对象，并提供一个 writeTo()
     * 方法用于把文件的字节流写到服务器的输出流
     *
     * @param contentType 文件的MimeType类型
     * @param file        目标文件
     */
    public static RequestBody create(final @Nullable MediaType contentType, final File file) {
        if (file == null) throw new NullPointerException("content == null");

        return new RequestBody() {
            @Override
            public @Nullable
            MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            /**
             * 通过该方法可以把文件写到服务器
             *
             * @param sink 服务器的输出流
             */
            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                // 这个是okio的一个类似字节流的一个类
                Source source = null;
                try {
                    // 通过 Okio.source()方法实现将文件转换成字节流
                    source = Okio.source(file);
                    // 将文件的字节流写到服务器
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}
