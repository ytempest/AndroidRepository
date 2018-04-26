package com.ytempest.rxjavaanalysis.rxjava.observable;

import com.ytempest.rxjavaanalysis.rxjava.Observable;
import com.ytempest.rxjavaanalysis.rxjava.Observer;

/**
 * @author ytempest
 *         Description：
 */
public class ObservableJust<T> extends Observable<T> {

    private final T value;

    public ObservableJust(T value) {
        this.value = value;
    }

    @Override
    protected void subscribeActual(Observer<T> observer) {
        ScalarDisposable sd = new ScalarDisposable<T>(observer, value);
        observer.onSubscribe();
        sd.run();
    }
}
