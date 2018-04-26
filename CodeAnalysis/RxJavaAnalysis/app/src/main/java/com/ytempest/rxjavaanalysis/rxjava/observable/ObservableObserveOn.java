package com.ytempest.rxjavaanalysis.rxjava.observable;

import com.ytempest.rxjavaanalysis.rxjava.Observable;
import com.ytempest.rxjavaanalysis.rxjava.Observer;
import com.ytempest.rxjavaanalysis.rxjava.scheduler.Scheduler;

/**
 * @author ytempest
 *         Description：
 */
public class ObservableObserveOn<T> extends Observable<T> {

    private final Observable<T> observable;
    private final Scheduler scheduler;

    public ObservableObserveOn(Observable<T> observable, Scheduler scheduler) {
        this.observable = observable;
        this.scheduler = scheduler;
    }

    @Override
    protected void subscribeActual(Observer<T> observer) {
        observable.subscribe(new ObserveOnObserver<T>(scheduler, observer));
    }


    static class ObserveOnObserver<T> implements Observer<T>, Runnable {

        private final Scheduler scheduler;
        private final Observer<T> observer;
        private Throwable error;
        private SimpleQueue<T> queue;
        private boolean done;

        ObserveOnObserver(Scheduler scheduler, Observer<T> observer) {
            this.scheduler = scheduler;
            this.observer = observer;
            queue = new SimpleQueue<T>();
        }

        @Override
        public void onSubscribe() {
            observer.onSubscribe();
        }

        @Override
        public void onNext(T t) {
            queue.offer(t);
            schedule();
        }

        @Override
        public void onError(Throwable e) {
            this.error = e;
            schedule();
        }

        @Override
        public void onComplete() {
            this.done = true;
            schedule();
        }

        private void schedule() {
            scheduler.schedule(this);
        }

        @Override
        public void run() {
            SimpleQueue<T> q = queue;

            for (; ; ) {
                if (q.isEmpty()) {
                    break;
                }
                try {
                    T value = q.poll();
                    observer.onNext(value);
                } catch (Exception e) {
                    observer.onError(e);
                }
            }


            if (error != null) {
                observer.onError(error);
                return;
            }

            if (done) {
                done = false;
                observer.onComplete();
            }

        }
    }
}

