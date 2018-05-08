package com.ytempest.okhttpanalysis.interceptor_analysis.cache;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description：
 */
public class CacheResponseInterceptor implements Interceptor {


    private static final String TAG = "CacheResponse";

    @Override
    public Response intercept(Chain chain) throws IOException {

        // 获取拦截到 response
        Response response = chain.proceed(chain.request());

        CacheControl cacheControl = response.cacheControl();
        if (cacheControl != null) {
            Log.e(TAG, "intercept before change : " + cacheControl.toString());
            String header = changeMaxAge(cacheControl.toString(), 70);
            if (header != null) {
                Log.e(TAG, "intercept after change : " + header);
                // 为response重新设置过期时间
                response = response.newBuilder()
                        .removeHeader("Cache-Control")
                        .addHeader("Cache-Control", header)
                        .build();
            }
        }

        return response;
    }

    /**
     * 只更改CacheControl响应头中的过期时间，不会更改其他内容
     * 如果这个响应头没有过期时间这一个type，那么就直接返回null
     */
    private String changeMaxAge(String string, int seconds) {
        if (TextUtils.isEmpty(string)) {
            return "max-age=" + seconds;
        }

        int start = string.indexOf("max-age=");
        if (start != -1) {
            int end = string.indexOf(",", start);
            if (end == -1) {
                end = string.length();
            }
            String originalStr = string.substring(start, end);
            return string.replace(originalStr, "max-age=" + seconds);

        }
        return null;
    }
}
