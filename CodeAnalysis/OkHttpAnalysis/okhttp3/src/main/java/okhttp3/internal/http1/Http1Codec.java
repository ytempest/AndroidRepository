/*
 * Copyright (C) 2012 The Android Open Source Project
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
package okhttp3.internal.http1;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RealResponseBody;
import okhttp3.internal.http.RequestLine;
import okhttp3.internal.http.StatusLine;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingTimeout;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static okhttp3.internal.Util.checkOffsetAndCount;
import static okhttp3.internal.http.StatusLine.HTTP_CONTINUE;

/**
 * A socket connection that can be used to send HTTP/1.1 messages. This class strictly enforces the
 * following lifecycle:
 * <p>
 * <ol>
 * <li>{@linkplain #writeRequest Send request headers}.
 * <li>Open a sink to write the request body. Either {@linkplain #newFixedLengthSink
 * fixed-length} or {@link #newChunkedSink chunked}.
 * <li>Write to and then close that sink.
 * <li>{@linkplain #readResponseHeaders Read response headers}.
 * <li>Open a source to read the response body. Either {@linkplain #newFixedLengthSource
 * fixed-length}, {@linkplain #newChunkedSource chunked} or {@linkplain
 * #newUnknownLengthSource unknown length}.
 * <li>Read from and close that source.
 * </ol>
 * <p>
 * <p>Exchanges that do not have a request body may skip creating and closing the request body.
 * Exchanges that do not have a response body can call {@link #newFixedLengthSource(long)
 * newFixedLengthSource(0)} and may skip reading and closing that source.
 */
public final class Http1Codec implements HttpCodec {
    private static final int STATE_IDLE = 0; // Idle connections are ready to write request headers.
    private static final int STATE_OPEN_REQUEST_BODY = 1;
    private static final int STATE_WRITING_REQUEST_BODY = 2;
    private static final int STATE_READ_RESPONSE_HEADERS = 3;
    private static final int STATE_OPEN_RESPONSE_BODY = 4;
    private static final int STATE_READING_RESPONSE_BODY = 5;
    private static final int STATE_CLOSED = 6;
    private static final int HEADER_LIMIT = 256 * 1024;

    /**
     * The client that configures this stream. May be null for HTTPS proxy tunnels.
     */
    final OkHttpClient client;
    /**
     * The stream allocation that owns this stream. May be null for HTTPS proxy tunnels.
     */
    final StreamAllocation streamAllocation;

    final BufferedSource source;
    final BufferedSink sink;
    int state = STATE_IDLE;
    private long headerLimit = HEADER_LIMIT;

    public Http1Codec(OkHttpClient client, StreamAllocation streamAllocation, BufferedSource source,
                      BufferedSink sink) {
        this.client = client;
        this.streamAllocation = streamAllocation;
        this.source = source;
        this.sink = sink;
    }

    /**
     * 如果请求体的长度可知，那么创建一个 Http1Codec.ChunkedSink对象，并返回；{@link Http1Codec.ChunkedSink}
     * 如果请求体的长度未知，那么创建一个 Http1Codec.FixedLengthSink对象，然后返回；{@link Http1Codec.FixedLengthSink}
     */
    @Override
    public Sink createRequestBody(Request request, long contentLength) {
        // 1、如果请求体的头信息存在 Transfer-Encoding:chunked头信息，说明这个RequestBody的长度未知，即为-1
        if ("chunked".equalsIgnoreCase(request.header("Transfer-Encoding"))) {
            // Stream a request body of unknown length.
            // 最终会创建一个 Http1Codec.ChunkedSink对象，这个对象持有服务器的输出流引用Sink，最后return这个对象
            return newChunkedSink();
        }

        // 2、如果请求体长度可知
        if (contentLength != -1) {
            // Stream a request body of a known length.
            return newFixedLengthSink(contentLength);
        }

        throw new IllegalStateException(
                "Cannot stream a request body without chunked encoding or a known content length!");
    }

    @Override
    public void cancel() {
        RealConnection connection = streamAllocation.connection();
        if (connection != null) connection.cancel();
    }

    /**
     * Prepares the HTTP headers and sends them to the server.
     * <p>
     * <p>For streaming requests with a body, headers must be prepared <strong>before</strong> the
     * output stream has been written to. Otherwise the body would need to be buffered!
     * <p>
     * <p>For non-streaming requests with a body, headers must be prepared <strong>after</strong> the
     * output stream has been written to and closed. This ensures that the {@code Content-Length}
     * header field receives the proper value.
     */
    @Override
    public void writeRequestHeaders(Request request) throws IOException {
        // 获取请求的状态行，如：GET / HTTP/1.1
        String requestLine = RequestLine.get(
                request, streamAllocation.connection().route().proxy().type());
        writeRequest(request.headers(), requestLine);
    }

    /**
     * 根据服务器返回的响应体的长度是否可知，使用 ChunkedSource或者FixedLengthSource封装服务器返回的
     * 输入流，然后再构建一个 RealResponseBody并返回
     *
     * @param response 已经从服务器的输入流中获取了相关头信息的Response
     * @return 一个 RealResponseBody对象
     */
    @Override
    public ResponseBody openResponseBody(Response response) throws IOException {
        streamAllocation.eventListener.responseBodyStart(streamAllocation.call);
        String contentType = response.header("Content-Type");

        if (!HttpHeaders.hasBody(response)) {
            Source source = newFixedLengthSource(0);
            return new RealResponseBody(contentType, 0, Okio.buffer(source));
        }

        // 1、如果服务器返回的响应体的长度不可知，也就是存在 Transfer-Encoding:chunked头信息
        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            // 创建一个 ChunkedSource封装服务器返回的输入流
            Source source = newChunkedSource(response.request().url());
            return new RealResponseBody(contentType, -1L, Okio.buffer(source));
        }

        // 2、如果服务器返回的响应体长度可知
        long contentLength = HttpHeaders.contentLength(response);
        if (contentLength != -1) {
            // 创建一个 FixedLengthSource封装服务器返回的输入流
            Source source = newFixedLengthSource(contentLength);
            return new RealResponseBody(contentType, contentLength, Okio.buffer(source));
        }

        // 3、其他情况
        return new RealResponseBody(contentType, -1L, Okio.buffer(newUnknownLengthSource()));
    }

    /**
     * Returns true if this connection is closed.
     */
    public boolean isClosed() {
        return state == STATE_CLOSED;
    }

    @Override
    public void flushRequest() throws IOException {
        sink.flush();
    }

    @Override
    public void finishRequest() throws IOException {
        sink.flush();
    }

    /**
     * Returns bytes of a request header for sending on an HTTP transport.
     * 将requestLine请求状态行、Request的头信息写到服务器的输出流中
     */
    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (state != STATE_IDLE) throw new IllegalStateException("state: " + state);
        sink.writeUtf8(requestLine).writeUtf8("\r\n");
        for (int i = 0, size = headers.size(); i < size; i++) {
            sink.writeUtf8(headers.name(i))
                    .writeUtf8(": ")
                    .writeUtf8(headers.value(i))
                    .writeUtf8("\r\n");
        }
        sink.writeUtf8("\r\n");
        state = STATE_OPEN_REQUEST_BODY;
    }

    /**
     * Parses bytes of a response header from an HTTP transport.
     * 该方法会调用readHeaderLine()方法从服务器的输入流中读取响应头信息，然后使用这些信息构建一个
     * Response.Builder对象，并返回这个对象
     *
     * @param expectContinue 如果设置为true则表示如果这是一个带有 "100"的中间响应码，那么则返回null
     *                       否则，这个方法永远不会返回null；（也就是说只有设置为true同时响应码为100才会返回null）
     */
    @Override
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        if (state != STATE_OPEN_REQUEST_BODY && state != STATE_READ_RESPONSE_HEADERS) {
            throw new IllegalStateException("state: " + state);
        }

        try {
            // 从服务器的输入流中读取响应的状态行，如：HTTP/1.1 200 OK、HTTP/1.1 206 Partial Content
            StatusLine statusLine = StatusLine.parse(readHeaderLine());

            // 使用状态行的信息 和 所有的头信息 构建Response
            Response.Builder responseBuilder = new Response.Builder()
                    .protocol(statusLine.protocol)
                    .code(statusLine.code)
                    .message(statusLine.message)
                    // 调用readHeaders()方法读取并获取所有的响应头信息
                    .headers(readHeaders());

            // 在设置了expectContinue为true以及响应状态码为100的时候才会调用
            if (expectContinue && statusLine.code == HTTP_CONTINUE) {
                return null;

            } else if (statusLine.code == HTTP_CONTINUE) {
                // 这里表示响应状态码为100的时候会调用
                state = STATE_READ_RESPONSE_HEADERS;
                return responseBuilder;
            }

            state = STATE_OPEN_RESPONSE_BODY;
            return responseBuilder;
        } catch (EOFException e) {
            // Provide more context if the server ends the stream before sending a response.
            IOException exception = new IOException("unexpected end of stream on " + streamAllocation);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * 以headerLimit作为索引从服务器的输入流中读取指定的响应头的信息，该索引的头信息；响应头信息如下：
     * HTTP/1.1 200 OK
     * Content-Type: application/vnd.android.package-archive
     * Date: Tue, 11 Sep 2018 15:43:56 GMT
     * Last-Modified: Fri, 04 May 2018 07:52:03 GMT
     * Accept-Ranges: bytes
     * Connection: keep-alive
     */
    private String readHeaderLine() throws IOException {
        String line = source.readUtf8LineStrict(headerLimit);
        headerLimit -= line.length();
        return line;
    }

    /**
     * Reads headers or trailers.
     * 遍历获取响应头中的所有头信息，并将这些信息封装成Headers对象，然后返回
     */
    public Headers readHeaders() throws IOException {
        Headers.Builder headers = new Headers.Builder();
        // parse the result headers until the first blank line
        for (String line; (line = readHeaderLine()).length() != 0; ) {
            Internal.instance.addLenient(headers, line);
        }
        return headers.build();
    }

    public Sink newChunkedSink() {
        if (state != STATE_OPEN_REQUEST_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_WRITING_REQUEST_BODY;
        return new ChunkedSink();
    }

    public Sink newFixedLengthSink(long contentLength) {
        if (state != STATE_OPEN_REQUEST_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_WRITING_REQUEST_BODY;
        return new FixedLengthSink(contentLength);
    }

    public Source newFixedLengthSource(long length) throws IOException {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_READING_RESPONSE_BODY;
        return new FixedLengthSource(length);
    }

    public Source newChunkedSource(HttpUrl url) throws IOException {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        state = STATE_READING_RESPONSE_BODY;
        return new ChunkedSource(url);
    }

    public Source newUnknownLengthSource() throws IOException {
        if (state != STATE_OPEN_RESPONSE_BODY) throw new IllegalStateException("state: " + state);
        if (streamAllocation == null) throw new IllegalStateException("streamAllocation == null");
        state = STATE_READING_RESPONSE_BODY;
        streamAllocation.noNewStreams();
        return new UnknownLengthSource();
    }

    /**
     * Sets the delegate of {@code timeout} to {@link Timeout#NONE} and resets its underlying timeout
     * to the default configuration. Use this to avoid unexpected sharing of timeouts between pooled
     * connections.
     */
    void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }

    /**
     * An HTTP body with a fixed length known in advance.
     * Description：当Request的RequestBody的长度可知时，会用该类对服务器的输出流进行第二次封装（第一
     * 次是封装成Sink，位置在{@link RealConnection#connectSocket(int, int, Call, EventListener)}）
     */
    private final class FixedLengthSink implements Sink {
        private final ForwardingTimeout timeout = new ForwardingTimeout(sink.timeout());
        private boolean closed;
        private long bytesRemaining;

        FixedLengthSink(long bytesRemaining) {
            this.bytesRemaining = bytesRemaining;
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }

        /**
         * FixedLengthSink会代理最初封装了服务器输出流的Sink，也就是这个方法会调用 Sink的write()方法
         * 第一次封装服务器输出流成Sink的位置：
         * {@link RealConnection#connectSocket(int, int, Call, EventListener)}
         * {@link Okio#sink(OutputStream, Timeout)}
         */
        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            checkOffsetAndCount(source.size(), 0, byteCount);
            if (byteCount > bytesRemaining) {
                throw new ProtocolException("expected " + bytesRemaining
                        + " bytes but received " + byteCount);
            }
            sink.write(source, byteCount);
            bytesRemaining -= byteCount;
        }

        @Override
        public void flush() throws IOException {
            if (closed)
                return; // Don't throw; this stream might have been closed on the caller's behalf.
            sink.flush();
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            closed = true;
            if (bytesRemaining > 0) throw new ProtocolException("unexpected end of stream");
            detachTimeout(timeout);
            state = STATE_READ_RESPONSE_HEADERS;
        }
    }

    /**
     * An HTTP body with alternating chunk sizes and chunk bodies. It is the caller's responsibility
     * to buffer chunks; typically by using a buffered sink with this sink.
     * Description：当Request的RequestBody的长度未知，即为-1时，会用该类对服务器的输出流进行第三次封
     * 装
     * （第一次是封装成Sink，第二次是使用RealBufferedSink封装Sink，封装的位置都在
     * {@link RealConnection#connectSocket(int, int, Call, EventListener)}）
     */
    private final class ChunkedSink implements Sink {
        private final ForwardingTimeout timeout = new ForwardingTimeout(sink.timeout());
        private boolean closed;

        ChunkedSink() {
        }

        @Override
        public Timeout timeout() {
            return timeout;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            if (closed) throw new IllegalStateException("closed");
            if (byteCount == 0) return;

            sink.writeHexadecimalUnsignedLong(byteCount);
            sink.writeUtf8("\r\n");
            sink.write(source, byteCount);
            sink.writeUtf8("\r\n");
        }

        @Override
        public synchronized void flush() throws IOException {
            if (closed)
                return; // Don't throw; this stream might have been closed on the caller's behalf.
            sink.flush();
        }

        @Override
        public synchronized void close() throws IOException {
            if (closed) return;
            closed = true;
            sink.writeUtf8("0\r\n\r\n");
            detachTimeout(timeout);
            state = STATE_READ_RESPONSE_HEADERS;
        }
    }

    private abstract class AbstractSource implements Source {
        protected final ForwardingTimeout timeout = new ForwardingTimeout(source.timeout());
        protected boolean closed;
        protected long bytesRead = 0;

        @Override
        public Timeout timeout() {
            return timeout;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            try {
                long read = source.read(sink, byteCount);
                if (read > 0) {
                    bytesRead += read;
                }
                return read;
            } catch (IOException e) {
                endOfInput(false, e);
                throw e;
            }
        }

        /**
         * Closes the cache entry and makes the socket available for reuse. This should be invoked when
         * the end of the body has been reached.
         */
        protected final void endOfInput(boolean reuseConnection, IOException e) throws IOException {
            if (state == STATE_CLOSED) return;
            if (state != STATE_READING_RESPONSE_BODY)
                throw new IllegalStateException("state: " + state);

            detachTimeout(timeout);

            state = STATE_CLOSED;
            if (streamAllocation != null) {
                streamAllocation.streamFinished(!reuseConnection, Http1Codec.this, bytesRead, e);
            }
        }
    }

    /**
     * An HTTP body with a fixed length specified in advance.
     */
    private class FixedLengthSource extends AbstractSource {
        private long bytesRemaining;

        FixedLengthSource(long length) throws IOException {
            bytesRemaining = length;
            if (bytesRemaining == 0) {
                endOfInput(true, null);
            }
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (bytesRemaining == 0) return -1;

            long read = super.read(sink, Math.min(bytesRemaining, byteCount));
            if (read == -1) {
                ProtocolException e = new ProtocolException("unexpected end of stream");
                endOfInput(false, e); // The server didn't supply the promised content length.
                throw e;
            }

            bytesRemaining -= read;
            if (bytesRemaining == 0) {
                endOfInput(true, null);
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            if (closed) return;

            if (bytesRemaining != 0 && !Util.discard(this, DISCARD_STREAM_TIMEOUT_MILLIS, MILLISECONDS)) {
                endOfInput(false, null);
            }

            closed = true;
        }
    }

    /**
     * An HTTP body with alternating chunk sizes and chunk bodies.
     */
    private class ChunkedSource extends AbstractSource {
        private static final long NO_CHUNK_YET = -1L;
        private final HttpUrl url;
        private long bytesRemainingInChunk = NO_CHUNK_YET;
        private boolean hasMoreChunks = true;

        ChunkedSource(HttpUrl url) {
            this.url = url;
        }

        @Override
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (!hasMoreChunks) return -1;

            if (bytesRemainingInChunk == 0 || bytesRemainingInChunk == NO_CHUNK_YET) {
                readChunkSize();
                if (!hasMoreChunks) return -1;
            }

            long read = super.read(sink, Math.min(byteCount, bytesRemainingInChunk));
            if (read == -1) {
                ProtocolException e = new ProtocolException("unexpected end of stream");
                endOfInput(false, e); // The server didn't supply the promised chunk length.
                throw e;
            }
            bytesRemainingInChunk -= read;
            return read;
        }

        private void readChunkSize() throws IOException {
            // Read the suffix of the previous chunk.
            if (bytesRemainingInChunk != NO_CHUNK_YET) {
                source.readUtf8LineStrict();
            }
            try {
                bytesRemainingInChunk = source.readHexadecimalUnsignedLong();
                String extensions = source.readUtf8LineStrict().trim();
                if (bytesRemainingInChunk < 0 || (!extensions.isEmpty() && !extensions.startsWith(";"))) {
                    throw new ProtocolException("expected chunk size and optional extensions but was \""
                            + bytesRemainingInChunk + extensions + "\"");
                }
            } catch (NumberFormatException e) {
                throw new ProtocolException(e.getMessage());
            }
            if (bytesRemainingInChunk == 0L) {
                hasMoreChunks = false;
                HttpHeaders.receiveHeaders(client.cookieJar(), url, readHeaders());
                endOfInput(true, null);
            }
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            if (hasMoreChunks && !Util.discard(this, DISCARD_STREAM_TIMEOUT_MILLIS, MILLISECONDS)) {
                endOfInput(false, null);
            }
            closed = true;
        }
    }

    /**
     * An HTTP message body terminated by the end of the underlying stream.
     */
    private class UnknownLengthSource extends AbstractSource {
        private boolean inputExhausted;

        UnknownLengthSource() {
        }

        @Override
        public long read(Buffer sink, long byteCount)
                throws IOException {
            if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            if (closed) throw new IllegalStateException("closed");
            if (inputExhausted) return -1;

            long read = super.read(sink, byteCount);
            if (read == -1) {
                inputExhausted = true;
                endOfInput(true, null);
                return -1;
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            if (closed) return;
            if (!inputExhausted) {
                endOfInput(false, null);
            }
            closed = true;
        }
    }
}
