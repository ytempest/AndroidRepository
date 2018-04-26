package com.ytempest.rxjavaanalysis.rxjava;

import com.ytempest.rxjavaanalysis.rxjava.map.Function;
import com.ytempest.rxjavaanalysis.rxjava.map.ObservableMap;
import com.ytempest.rxjavaanalysis.rxjava.observable.ObservableJust;
import com.ytempest.rxjavaanalysis.rxjava.observable.ObservableObserveOn;
import com.ytempest.rxjavaanalysis.rxjava.observable.ObservableSubscribeOn;
import com.ytempest.rxjavaanalysis.rxjava.scheduler.Scheduler;



/**
 * @author ytempest
 *         Description：
 */
public abstract class Observable<T> implements ObservableSource<T> {

    public static <T> Observable<T> just(T item) {

        return onAssembly(new ObservableJust<T>(item));
    }

    public <R> Observable<R> map(Function<T, R> function) {

        return onAssembly(new ObservableMap<T, R>(this, function));
    }


    public Observable<T> subscribeOn(Scheduler scheduler) {

        return onAssembly(new ObservableSubscribeOn<T>(this, scheduler));
    }

    public Observable<T> observeOn(Scheduler scheduler) {

        return onAssembly(new ObservableObserveOn<T>(this, scheduler));
    }

    @Override
    public void subscribe(Observer<T> observer) {
        subscribeActual(observer);
    }

    protected abstract void subscribeActual(Observer<T> observer);

    private static <T> Observable<T> onAssembly(Observable<T> observer) {
        return observer;
    }
}
