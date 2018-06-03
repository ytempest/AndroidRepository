package com.ytempest.retrofitanalysis.sample3.imitate;

import android.util.Log;

import java.io.IOException;

import okhttp3.*;


/**
 * @author ytempest
 *         Description：
 */
class OkHttpCall<T> implements Call<T> {

    private static final String TAG = "OkHttpCall";

    private final ServiceMethod<T, Object> mServiceMethod;
    private final Object[] mArgs;

    OkHttpCall(ServiceMethod<T, Object> serviceMethod, Object[] args) {
        this.mServiceMethod = serviceMethod;
        this.mArgs = args;
    }

    @Override
    public void enqueue(final Callback<T> callBack) {
        okhttp3.Call rawCall = createRawCall();
        if (rawCall != null) {
            rawCall.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callBack.onFailure(OkHttpCall.this, e);
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) throws IOException {
                    Response response = parseRawResponse(rawResponse);
                    callBack.onResponse(OkHttpCall.this, response);
                }
            });
        }
    }

    private Response<T> parseRawResponse(okhttp3.Response rawResponse) throws IOException {
        // 获取最初的ResponseBody
        ResponseBody rawBody = rawResponse.body();

        int resultCode = rawResponse.code();

        if (resultCode < 200 || resultCode >= 300) {
            return Response.error(rawResponse, rawBody);
        }

        if (resultCode == 204 || resultCode == 205) {
            rawBody.close();
            return Response.success(rawResponse, null);
        }

        // 对响应结果进行分类解析
        T body = mServiceMethod.toResponse(rawBody);
        return Response.success(rawResponse, body);
    }

    private okhttp3.Call createRawCall() {
        try {
            return mServiceMethod.toCall(mArgs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
