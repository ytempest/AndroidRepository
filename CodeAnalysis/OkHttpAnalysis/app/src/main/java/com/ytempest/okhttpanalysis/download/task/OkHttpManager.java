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
    private static OkHttpManager sInstance;
    private static OkHttpClient mOkHttpClient;

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

    public Call asyncCall(String url) {
        Request request = new Request.Builder().url(url).build();
        return mOkHttpClient.newCall(request);
    }

    public Response asyncCall(String url, long start, long end) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Range", "bytes=" + start + "-" + end)
                .build();

        return mOkHttpClient.newCall(request).execute();
    }
}
