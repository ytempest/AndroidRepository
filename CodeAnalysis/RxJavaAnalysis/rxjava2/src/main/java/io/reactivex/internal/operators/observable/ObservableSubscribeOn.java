/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.internal.operators.observable;


import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;

/**
 * Description：这是一个实现了将代理的 Observable对象放在指定线程执行的 Observable类；
 * 它的功能就只有这一个
 *
 * @param <T> 被观察元素的类型
 */
public final class ObservableSubscribeOn<T> extends AbstractObservableWithUpstream<T, T> {
    /**
     * 调度的目的线程，如：Schedulers.io()、AndroidSchedulers.mainThread()
     */
    final Scheduler scheduler;

    /**
     * @param source    被观察者对象
     * @param scheduler 调度的目的线程
     */
    public ObservableSubscribeOn(ObservableSource<T> source, Scheduler scheduler) {
        super(source);
        this.scheduler = scheduler;
    }

    /**
     * 当调用这个对象的 subscribe()方法就会调用到这个 subscribeActual()方法
     *
     * @param s 观察者对象
     */
    @Override
    public void subscribeActual(final Observer<? super T> s) {
        // 使用 SubscribeOnObserver封装下一个观察者对象，用于处理一些问题
        final SubscribeOnObserver<T> parent = new SubscribeOnObserver<T>(s);

        // 回调观察者的 onSubscribe() 方法，可以知道 onSubscribe()方法只会在原线程中执行
        s.onSubscribe(parent);

        // scheduler：指定线程，如：IoScheduler
        // new SubscribeTask(parent)：执行单元，把需要在指定线程执行的逻辑放在这个类的 run() 方法中
        // 这段代码的作用是：创建一个线程调度的执行单元，然后把这个执行单元交由指定Scheduler分发处理
        parent.setDisposable(scheduler.scheduleDirect(new SubscribeTask(parent)));
    }


    /**
     * Description：这个类是线程调度中观察者对象的封装类；当调度线程的时候，会把对象该线程交给
     * 目的线程执行观察者对象的方法
     * 可以发现：这个类使用了代理模式，对观察者方法进行代理
     */
    static final class SubscribeOnObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {

        private static final long serialVersionUID = 8094547886072529208L;
        // 真正的 观察者对象
        final Observer<? super T> actual;

        final AtomicReference<Disposable> s;

        SubscribeOnObserver(Observer<? super T> actual) {
            this.actual = actual;
            this.s = new AtomicReference<Disposable>();
        }

        @Override
        public void onSubscribe(Disposable s) {
            DisposableHelper.setOnce(this.s, s);
        }

        @Override
        public void onNext(T t) {
            actual.onNext(t);
        }

        @Override
        public void onError(Throwable t) {
            actual.onError(t);
        }

        @Override
        public void onComplete() {
            actual.onComplete();
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(s);
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }

        void setDisposable(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }
    }

    /**
     * Description：这个类是 subscribeOn() 方法线程调度的一个执行单元；这个类会封装了被观察对象
     * 以及其观察者对象，然后把这个执行逻辑（即：source.subscribe(parent)）放在 run() 方法中，然
     * 后这个 run() 方法会在指定的线程中执行
     */
    final class SubscribeTask implements Runnable {
        private final SubscribeOnObserver<T> parent;

        SubscribeTask(SubscribeOnObserver<T> parent) {
            this.parent = parent;
        }

        @Override
        public void run() {
            // source：这个是上一个 Observable
            // parent：观察者对象的一个 SubscribeOnObserver封装类
            // 只要把这段代码放在指定线程中执行，那么就实现了线程调度的作用
            source.subscribe(parent);
        }
    }
}
