package com.ytempest.okhttpanalysis.upload_analysis_1_2.solution_2;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

/**
 * @author ytempest
 *         Description：使用静态代理的方法代理MultipartBody，去监听上传过程
 */
public class MultipartBodyDelegate extends RequestBody {

    private MultipartBody mMultipartBody;
    private long mLength;
    private long mCurrentLength;

    public OnUploadListener mOnUploadListener;

    public MultipartBodyDelegate(MultipartBody multipartBody) {
        mMultipartBody = multipartBody;
    }

    public MultipartBodyDelegate(MultipartBody multipartBody, OnUploadListener onUploadListener) {
        mMultipartBody = multipartBody;
        mOnUploadListener = onUploadListener;
    }


    /**
     * 这个方法要进行重写，为了返回正确的RequestBody的类型
     */
    @Nullable
    @Override
    public MediaType contentType() {
        return mMultipartBody.contentType();
    }

    /**
     * 这个方法要进行重写，为了返回正确的RequestBody的长度
     */
    @Override
    public long contentLength() throws IOException {
        return mMultipartBody.contentLength();
    }



    /**
     * 调用这个方法向服务器写数据
     *
     * @param sink 这个是连接服务器的一个输出流，通过这个输出流可以向服务器输入数据
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (mLength == 0) {
            // 获取文件大小
            mLength = mMultipartBody.contentLength();
        }


        // 创建一个字节流写过程的代理，监听字节流的写操作过程
        ForwardingSink forwardingSink = new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                // 累加文件长度，用于标识进度
                mCurrentLength += byteCount;
                if (mOnUploadListener != null) {
                    // 上传进度回调
                    mOnUploadListener.onProgress(mLength, mCurrentLength);
                }
                super.write(source, byteCount);
            }
        };

        // 将 ForwardingSink 转换成 BufferedSink，因为 MultipartBody的writeTo方法只接受 BufferedSink
        BufferedSink bufferedSink = Okio.buffer(forwardingSink);

        // 让原来的被代理对象处理逻辑
        mMultipartBody.writeTo(bufferedSink);
    }

    public void setOnUploadListener(OnUploadListener onUploadListener) {
        mOnUploadListener = onUploadListener;
    }

    public interface OnUploadListener {
        void onProgress(long maxLength, long currentLength);
    }


}
