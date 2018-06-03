package com.ytempest.retrofitanalysis.sample3.imitate;

import android.support.annotation.Nullable;

import okhttp3.ResponseBody;

/**
 * @author ytempest
 *         Description：
 */
public class Response<T> {
    private okhttp3.Response rawResponse;
    private T body;
    private ResponseBody errorBody;

    public Response(okhttp3.Response rawResponse, T body, ResponseBody errorBody) {
        this.rawResponse = rawResponse;
        this.body = body;
        this.errorBody = errorBody;
    }

    public static <T> Response<T> success(okhttp3.Response rawResponse, @Nullable T body) {
        return new Response<>(rawResponse, body, null);
    }

    public static <T> Response<T> error(okhttp3.Response rawResponse, okhttp3.ResponseBody errorBody) {
        return new Response<>(null, null, errorBody);
    }

    public T body() {
        return body;
    }
}
