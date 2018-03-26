package com.ytempest.framelibrary.http;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.ytempest.baselibrary.http.EngineCallBack;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.http.IHttpEngine;
import com.ytempest.framelibrary.http.cache.CacheDataUtils;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description: OkHttp默认的引擎
 */
public class OkHttpEngine implements IHttpEngine {

    private static final String TAG = "OkHttpEngine";
    private static OkHttpClient mOkHttpClient = new OkHttpClient();
    private Handler mHandler = new Handler();

    @Override
    public void post(boolean cache, Context context, String url, Map<String, Object> params, final EngineCallBack callBack) {

        final String postUrl = HttpUtils.jointParams(url, params);
        // 打印，方便查看
        Log.e(TAG, "Post请求路径：" + postUrl);

        RequestBody requestBody = appendBody(params);
        Request request = new Request.Builder()
                .url(url)
                .tag(context)
                .post(requestBody)
                .build();

        mOkHttpClient.newCall(request).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // 回调自己定义的回调方法
                        callBack.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // 这个 两个回掉方法都不是在主线程中
                        String postResultJson = response.body().string();
                        Log.e(TAG, "Post返回结果：" + postResultJson);
                        callBack.onSuccess(postResultJson);
                    }
                }
        );
    }

    @Override
    public void get(final boolean cache, Context context, String url, Map<String, Object> params, final EngineCallBack callBack) {
        // 1.拼接请求路径  参数 + 路径代表唯一标识
        final String requestUrl = HttpUtils.jointParams(url, params);
        Log.e(TAG, "Get请求路径：" + requestUrl);

        // 2.判断需不需要缓存
        if (cache) {
            String cacheResultJson = CacheDataUtils.getCacheResultJson(requestUrl);
            // 2.1 如果缓存不为空，就直接回调执行成功的方法
            if (!TextUtils.isEmpty(cacheResultJson)) {
                Log.e(TAG, " --> 已经读到缓存");
                // 2.2 数据库有缓存,直接就去执行，里面执行成功
                callBack.onSuccess(cacheResultJson);
            }
        }

        Request.Builder requestBuilder = new Request.Builder().url(requestUrl).tag(context);
        //可以省略，默认是GET请求
        Request request = requestBuilder.build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            /**
             *  这个方法不是在主线程中执行的
             */
            @Override
            public void onFailure(Call call, final IOException e) {
                // 提交给主线程处理
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onError(e);
                    }
                });
            }

            /**
             *  这个方法不是在主线程中执行的
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 3. 获取服务器返回的数据
                final String resultJson = response.body().string();

                // 4. 判断是否需要缓存服务器返回的数据
                if (cache) {
                    String cacheResultJson = CacheDataUtils.getCacheResultJson(requestUrl);
                    if (!TextUtils.isEmpty(resultJson)) {
                        // 4.1 将服务器返回的数据和缓存在数据库的数据比较
                        if (resultJson.equals(cacheResultJson)) {
                            // 4.2 如果数据一样，不需要执行成功成功方法刷新界面
                            Log.e(TAG, "数据和缓存一致：" + resultJson);
                            return;
                        }
                    }
                }
                // 提交给主线程处理
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 5. 执行网络请求成功的方法
                        callBack.onSuccess(resultJson);
                    }
                });

                Log.e(TAG, "Get返回结果：" + resultJson);
                if (cache) {
                    // 4.3 数据不一样，缓存服务器返回的数据
                    CacheDataUtils.setCacheResultJson(requestUrl, resultJson);
                }
            }
        });
    }

    /**
     * 组装post请求参数RequestBody
     */
    protected RequestBody appendBody(Map<String, Object> params) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        addParams(builder, params);
        return builder.build();
    }

    /**
     * 添加参数
     */
    private void addParams(MultipartBody.Builder builder, Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, params.get(key) + "");
                Object value = params.get(key);
                if (value instanceof File) {
                    // 处理文件 --> Object File
                    File file = (File) value;
                    builder.addFormDataPart(key, file.getName(),
                            RequestBody.create(MediaType.parse(guessMimeType(file
                                    .getAbsolutePath())), file));
                } else if (value instanceof List) {
                    // 代表提交的是 List集合
                    try {
                        List listFiles = (List) value;
                        for (int i = 0; i < listFiles.size(); i++) {
                            // 获取文件
                            File file = (File) listFiles.get(i);
                            builder.addFormDataPart(key + i, file.getName(), RequestBody
                                    .create(MediaType.parse(guessMimeType(file
                                            .getAbsolutePath())), file));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    builder.addFormDataPart(key, value + "");
                }
            }
        }
    }

    /**
     * 猜测文件类型
     */
    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }
}
