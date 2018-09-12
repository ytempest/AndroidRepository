package com.ytempest.okhttpanalysis.interceptor_analysis.cache;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description：拦截 request 请求，判断是否有网，没有网络同时缓存没有过期，那么就会读取缓存
 */
public class CacheRequestInterceptor implements Interceptor {

    private Context mContext;

    public CacheRequestInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // 获取拦截到的 request
        Request request = chain.request();
        if (!isNetworkAvailable()) {
            // 如果网络不可用
            request = request.newBuilder()
                    // 当网络不可用的时候，设置只读缓存
                    // 缓存的读取还依赖于缓存的过期时间，如果已经过期，那么缓存就无法读取
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();

        }
        return chain.proceed(request);
    }

    /**
     * 判断网络是否可用
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
    }
}
