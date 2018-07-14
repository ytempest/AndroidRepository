package com.ytempest.framelibrary.net.listener;

/**
 * @author ytempest
 *         Description：
 */
public interface DataDisposeListener<T> {
    void onSucceed(T result);

    void onFailure(Throwable throwable);
}
