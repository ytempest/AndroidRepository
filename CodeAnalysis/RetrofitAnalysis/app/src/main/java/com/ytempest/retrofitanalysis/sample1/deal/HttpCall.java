package com.ytempest.retrofitanalysis.sample1.deal;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author ytempest
 *         Description：这个类用于解决请求后台数据成功和失败返回的数据不一致的情况
 */
public abstract class HttpCall<T> implements Callback<Result<T>> {

    @Override
    public void onResponse(Call<Result<T>> call, Response<Result<T>> response) {
        Result<T> result = response.body();
        if (result == null) {
            return;
        }
        if (!result.isOk()) {
            onError(result.getCode(), result.getMsg());
            return;
        }
        // 获取本类上的泛型
        Class<T> entityClazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        T data = new Gson().fromJson(result.getData().toString(), entityClazz);
        onSucceed(data);
    }

    @Override
    public void onFailure(Call<Result<T>> call, Throwable t) {
        // 这里解决连接网络失败，获取后台数据失败的逻辑
        t.printStackTrace();
    }


    protected abstract void onSucceed(T result);

    protected abstract void onError(String code, String msg);
}
