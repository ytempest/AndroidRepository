package com.ytempest.okhttpanalysis.download.task;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description：
 */
class OkHttpManager {
    private volatile static OkHttpManager sInstance;
    private OkHttpClient mOkHttpClient;

    private OkHttpManager() {
        mOkHttpClient = new OkHttpClient();
    }

    public static OkHttpManager getInstance() {
        if (sInstance == null) {
            synchronized (OkHttpManager.class) {
                if (sInstance == null) {
                    sInstance = new OkHttpManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 将传递进来的url使用OkHttp转换成一个 Call对象
     */
    public Call asyncCall(String url) {
        Request request = new Request.Builder().url(url).build();
        return mOkHttpClient.newCall(request);
    }

    /**
     * 将传递进来的文件下载地址url请求该文件从start字节到end字节之间的内容，然后返回该response
     */
    public Response asyncCall(String url, long start, long end) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=" + start + "-" + end)
                .build();

        return mOkHttpClient.newCall(request).execute();
    }
}
