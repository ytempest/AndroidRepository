package com.ytempest.rxjavaanalysis.rxjava.observable;

import com.ytempest.rxjavaanalysis.rxjava.Observer;

/**
 * @author ytempest
 *         Description：
 */
public class ScalarDisposable<T> implements Runnable {

    private final Observer<T> observer;
    private final T value;

    public  ScalarDisposable(Observer<T> observer, T value) {
        this.observer = observer;
        this.value = value;
    }

    @Override
    public void run() {
        try {
            observer.onNext(value);
            observer.onComplete();
        } catch (Exception e) {
            observer.onError(e);
        }
    }
}
