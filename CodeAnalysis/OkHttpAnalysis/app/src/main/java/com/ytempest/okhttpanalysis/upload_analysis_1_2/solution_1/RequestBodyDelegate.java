package com.ytempest.okhttpanalysis.upload_analysis_1_2.solution_1;


import android.support.annotation.Nullable;

import com.ytempest.okhttpanalysis.upload_analysis_1_2.listener.OnUploadListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

/**
 * @author ytempest
 *         Description：这个类实现了将实现的RequestBody的上传进度进行监听，在上传多个文件时，如果要监听
 *         每个文件的上传进度，那么可以使用RequestBodyDeledate代理每一个文件的 RequestBody，从而实现每
 *         一个文件的上传进度监听
 */
public class RequestBodyDelegate extends RequestBody {

    private final RequestBody mRequestBody;
    private long mCurrentLength;
    private OnUploadListener mOnUploadListener;

    public RequestBodyDelegate(RequestBody requestBody) {
        mRequestBody = requestBody;
    }


    public RequestBodyDelegate(RequestBody requestBody, OnUploadListener listener) {
        this.mRequestBody = requestBody;
        this.mOnUploadListener = listener;
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

        final long maxLength = mRequestBody.contentLength();

        // 创建一个字节流写过程的代理，监听字节流的写操作过程
        ForwardingSink forwardingSink = new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                // 累加文件长度，用于标识进度
                mCurrentLength += byteCount;
                if (mOnUploadListener != null) {
                    // 上传进度回调
                    mOnUploadListener.onProgress(maxLength, mCurrentLength);
                }
                super.write(source, byteCount);
            }
        };

        // 将 ForwardingSink 转换成 BufferedSink，因为 MultipartBody的writeTo方法只接受 BufferedSink
        BufferedSink bufferedSink = Okio.buffer(forwardingSink);

        // 让原来的被代理对象处理逻辑
        mRequestBody.writeTo(bufferedSink);

        // 刷新，保证最后的文件流内容能刷新都服务器中
        bufferedSink.flush();
    }


    public void setOnUploadListener(OnUploadListener onUploadListener) {
        mOnUploadListener = onUploadListener;
    }
}
