package com.ytempest.rxjavaanalysis.rxjava.observable;

import com.ytempest.rxjavaanalysis.rxjava.Observable;
import com.ytempest.rxjavaanalysis.rxjava.Observer;
import com.ytempest.rxjavaanalysis.rxjava.scheduler.Scheduler;

/**
 * @author ytempest
 *         Description：
 */
public class ObservableSubscribeOn<T> extends Observable<T> {


    private final Observable<T> observable;
    private final Scheduler scheduler;

    public ObservableSubscribeOn(Observable<T> observable, Scheduler scheduler) {
        this.observable = observable;
        this.scheduler = scheduler;
    }

    @Override
    protected void subscribeActual(Observer<T> observer) {
        // 代理观察者对象
        SubscribeOnObserver<T> subscribeOnObserver = new SubscribeOnObserver<T>(observer);

        // 封装要进行线程调度的执行单元
        SubscribeTask subscribeTask = new SubscribeTask(observable, subscribeOnObserver);

        scheduler.schedule(subscribeTask);
    }


    static class SubscribeOnObserver<T> implements Observer<T> {

        private final Observer<T> observer;

        SubscribeOnObserver(Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe() {
            observer.onSubscribe();
        }

        @Override
        public void onNext(T t) {
            observer.onNext(t);
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

    final class SubscribeTask implements Runnable {

        private final Observable<T> observable;
        private final Observer<T> observer ;

        SubscribeTask(Observable<T> observable, Observer<T> observer) {
            this.observable = observable;
            this.observer = observer;
        }

        @Override
        public void run() {
            observable.subscribe(observer);
        }
    }

}

