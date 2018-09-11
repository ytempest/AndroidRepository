package com.ytempest.okhttpanalysis.upload_analysis_1_2.solution_1;


import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import okio.Timeout;

public class RequestBodyDelegateTest extends RequestBody {

    private final RequestBody mRequestBody;

    public RequestBodyDelegateTest(RequestBody requestBody) {
        mRequestBody = requestBody;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        // 在这里实现 hook 服务器的输出流
        SinkDelegate sinkDelegate = new SinkDelegate(sink);

        BufferedSink buffer = Okio.buffer(sinkDelegate);

        mRequestBody.writeTo(buffer);
    }

    private class SinkDelegate implements Sink {

        private final BufferedSink mSink;

        private SinkDelegate(BufferedSink sink) {
            mSink = sink;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            mSink.write(source, byteCount);
        }

        @Override
        public void flush() throws IOException {
            mSink.flush();
        }

        @Override
        public Timeout timeout() {
            return mSink.timeout();
        }

        @Override
        public void close() throws IOException {
            mSink.close();
        }
    }

}
