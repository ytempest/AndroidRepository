package com.ytempest.retrofitanalysis.sample3.imitate;

/**
 * @author ytempest
 *         Description：
 */
public interface Callback<T> {
    void onResponse(Call<T> call, Response<T> response);

    void onFailure(Call<T> call, Throwable t);
}
