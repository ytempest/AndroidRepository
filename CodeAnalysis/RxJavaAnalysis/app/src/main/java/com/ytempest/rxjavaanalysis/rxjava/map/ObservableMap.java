package com.ytempest.rxjavaanalysis.rxjava.map;

import com.ytempest.rxjavaanalysis.rxjava.Observable;
import com.ytempest.rxjavaanalysis.rxjava.Observer;

/**
 * @author ytempest
 *         Description：
 */
public class ObservableMap<T, R> extends Observable<R> {

    private final Observable<T> observable;
    private final Function<T, R> function;

    public ObservableMap(Observable<T> observable, Function<T, R> function) {
        this.observable = observable;
        this.function = function;
    }

    @Override
    protected void subscribeActual(Observer<R> observer) {
        observable.subscribe(new MapObserver<T, R>(observer, function));
    }

    static class MapObserver<T, R> implements Observer<T> {
        private final Observer<R> observer;
        private final Function<T, R> function;

        MapObserver(Observer<R> observer, Function<T, R> function) {
            this.observer = observer;
            this.function = function;
        }

        @Override
        public void onSubscribe() {
            observer.onSubscribe();
        }

        @Override
        public void onNext(T t) {
            R value = function.call(t);
            observer.onNext(value);
        }

        @Override
        public void onError(Throwable e) {
            observer.onError(e);
        }

        @Override
        public void onComplete() {
            observer.onComplete();
        }
    }
}
