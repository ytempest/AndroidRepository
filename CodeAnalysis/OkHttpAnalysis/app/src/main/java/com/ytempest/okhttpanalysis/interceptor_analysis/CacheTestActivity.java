package com.ytempest.okhttpanalysis.interceptor_analysis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ytempest.okhttpanalysis.R;
import com.ytempest.okhttpanalysis.interceptor_analysis.cache.CacheRequestInterceptor;
import com.ytempest.okhttpanalysis.interceptor_analysis.cache.CacheResponseInterceptor;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CacheTestActivity extends AppCompatActivity {

    private static final String TAG = "CacheTestActivity";

    /**
     * 缓存路径
     */
    private File mCacheFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_test);


        mCacheFile = new File(getExternalCacheDir(), "cacheTest");
    }


    public void cacheTest(View view) {
        String url = "https://www.jianshu.com/p/3bdeee756159";

        // 设置缓存目录以及缓存的大小 10M
        Cache cache = new Cache(mCacheFile, 10 * 1024 * 1024);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                // 设置自定义拦截器，如果没有网络以及缓存没有过期，那么就会读缓存
                .addInterceptor(new CacheRequestInterceptor(CacheTestActivity.this))
                // 设置网络拦截器，设置后台返回的数据的缓存过期时间
                .addNetworkInterceptor(new CacheResponseInterceptor())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        // 开始request
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e(TAG, "onFailure: 下载失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 必须先调用这一条语句，不然就算本地有缓存也无法读取缓存
                // 如果有网络，那么realResult就是从网络获取的数据
                // 如果网络不可用，那么realResult就是从缓存获取的数据（如果缓存没过期）
                String realResult = response.body().string();
                Log.e(TAG, "onResponse: realResult：" + realResult);
                Log.e(TAG, "onResponse 本地缓存：" + response.cacheResponse());
                Log.e(TAG, "onResponse: 网络数据 ：" + response.networkResponse());
                Log.e(TAG, "onResponse: --------------------------------------");
            }
        });

    }

}
