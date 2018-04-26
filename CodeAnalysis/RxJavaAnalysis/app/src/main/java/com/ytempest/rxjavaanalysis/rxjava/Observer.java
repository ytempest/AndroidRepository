package com.ytempest.rxjavaanalysis.rxjava;

/**
 * @author ytempest
 *         Description：
 */
public interface Observer<T> {
    void onSubscribe();

    void onNext(T t);

    void onError(Throwable e);

    void onComplete();
}
