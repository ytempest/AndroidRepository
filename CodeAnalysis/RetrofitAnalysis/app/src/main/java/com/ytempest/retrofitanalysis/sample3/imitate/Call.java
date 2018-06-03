package com.ytempest.retrofitanalysis.sample3.imitate;

/**
 * @author ytempest
 *         Description：
 */
public interface Call<T> {
    void enqueue(Callback<T> callBack);
}
