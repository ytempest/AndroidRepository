package com.ytempest.framelibrary.net.response;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.ytempest.framelibrary.net.exception.OkHttpException;
import com.ytempest.framelibrary.net.listener.DataDisposeListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author ytempest
 *         Description：
 */
public class JsonCallback<T> extends BaseCallback {

    private DataDisposeListener<T> mListener;
    private Handler mDeliveryHandler;

    public JsonCallback(DataDisposeListener<T> listener) {
        this.mListener = listener;
        mDeliveryHandler = new Handler(Looper.getMainLooper());

    }

    @Override
    public void onFailure(Call call, final IOException e) {
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onFailure(new OkHttpException(NETWORK_ERROR, e.getMessage()));
            }
        });
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        final String result = response.body().string();
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                handleResponse(result);
            }


        });
    }

    private void handleResponse(String responseResult) {
        if (responseResult == null || responseResult.trim().equals("")) {
            mListener.onFailure(new OkHttpException(OTHER_CODE, "the result of response is empty"));
        }
        try {
            // 开始解析数据
            Class<T> entityClazz = (Class<T>) ((ParameterizedType) mListener.getClass().getGenericInterfaces()[0])
                    .getActualTypeArguments()[0];
            T resultObject = new Gson().fromJson(responseResult, entityClazz);
            mListener.onSucceed(resultObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
