package com.ytempest.framelibrary.net;

import android.net.SSLCertificateSocketFactory;
import android.util.Log;

import com.ytempest.framelibrary.net.cookie.SimpleCookieJar;
import com.ytempest.framelibrary.net.https.HttpsUtils;
import com.ytempest.framelibrary.net.listener.DataDisposeListener;
import com.ytempest.framelibrary.net.response.JsonCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description：
 */
public class OkHttpManager {
    private static final String TAG = "OkHttpManager";


    private final static int TIME_OUT = 30;
    private static OkHttpClient mOkHttpClient;

    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 设置连接超时

        builder.connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                // 读超时
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                // 写超时
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                // OkHttp默认支持重定向
                .followRedirects(true);

        // 主机名验证，这个在使用Https连接时才会用到
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                Log.e(TAG, "verify: 开始主机名验证");
                // 在这里做一些验证逻辑
                return true;
            }
        });

        // 为所有的url请求添加一个请求头，这个看业务需要，如果你的应用每一个请求都附带一个
        // 相同的键值对，那么可以在这里统一加到请求上
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                request = request.newBuilder()
//                        .addHeader("User-Agent", "Imooc-Mobile")
                        .build();
                return chain.proceed(request);
            }
        });

        // 为OkHttp设置一个保存response中可能存在的cookie以及为相应的request添加cookie的处理类
        builder.cookieJar(new SimpleCookieJar());

        // 设置一个为OkHttp创建SSL的Socket连接对象的SocketFactory
        builder.sslSocketFactory(HttpsUtils.initSSLSocketFactory(), HttpsUtils.initTrustManager());

        mOkHttpClient = builder.build();
    }

    private OkHttpManager() {
    }


    public static OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public static <T> Call get(Request request, DataDisposeListener<T> listener) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new JsonCallback<T>(listener));
        return call;
    }


    public static Call post(Request request, Callback callback) {
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }


}
