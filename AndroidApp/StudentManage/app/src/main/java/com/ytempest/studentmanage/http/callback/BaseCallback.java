package com.ytempest.studentmanage.http.callback;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * @author ytempest
 *         Description：
 */
public abstract class BaseCallback<T> implements Callback<T> {

    private static final String TAG = "BaseCallback";
    private Gson gson;
    private Handler handler;

    public BaseCallback() {
        gson = new Gson();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        final T result = response.body();

        // 如果是InputStream就不需要切换到主线程
        if (result instanceof InputStream) {
            onSuccess(result);
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onSuccess(result);
                }
            });
        }

    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        call.request().url().toString();
        Log.e(TAG, "onFailure: " + call.request().url().toString());
    }

    /**
     * 返回可以直接操作的对象
     *
     * @param result 可以直接操作的对象
     */
    public abstract void onSuccess(T result);
}