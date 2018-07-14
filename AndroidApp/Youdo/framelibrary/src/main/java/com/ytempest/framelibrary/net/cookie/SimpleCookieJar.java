package com.ytempest.framelibrary.net.cookie;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @author ytempest
 *         Description：
 */
public class SimpleCookieJar implements CookieJar {
    private static final String TAG = "SimpleCookieJar";

    private final List<Cookie> allCookies = new ArrayList<>();

    /**
     * 这个方法用于保存所有服务器返回的结果中可能存在的cookie
     */
    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        Log.e(TAG, "saveFromResponse: 开始保存Cookie");
        for (Cookie cookie : cookies) {
            Log.e(TAG, "saveFromResponse: 保存的Cookie名称=" + cookie.name());
        }
        allCookies.addAll(cookies);
    }

    /**
     * 这个方法用于为url请求添加相应的cookie，但是有可能这个url请求不会添加任何cookie
     */
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        Log.e(TAG, "loadForRequest: 开始加载Cookie");
        List<Cookie> result = new ArrayList<>();
        for (Cookie cookie : allCookies) {
            if (cookie.matches(url)) {
                Log.e(TAG, "loadForRequest: 加载的cookie名称=" + cookie.name());
                result.add(cookie);
            }
        }
        return result;
    }
}
