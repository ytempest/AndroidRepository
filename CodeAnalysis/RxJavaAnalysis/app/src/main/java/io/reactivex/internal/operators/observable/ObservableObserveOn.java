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

import io.reactivex.*;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.fuseable.*;
import io.reactivex.internal.observers.BasicIntQueueDisposable;
import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.internal.schedulers.TrampolineScheduler;
import io.reactivex.plugins.RxJavaPlugins;

public final class ObservableObserveOn<T> extends AbstractObservableWithUpstream<T, T> {
    final Scheduler scheduler;
    final boolean delayError;
    final int bufferSize;

    public ObservableObserveOn(ObservableSource<T> source, Scheduler scheduler, boolean delayError, int bufferSize) {
        super(source);
        this.scheduler = scheduler;
        this.delayError = delayError;
        this.bufferSize = bufferSize;
    }

    /**
     * 这个方法是重点
     *
     * @param observer 观察者
     */
    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        if (scheduler instanceof TrampolineScheduler) {
            source.subscribe(observer);
        } else {

            /**
             * scheduler：一个 HandlerScheduler 对象 {@link io.reactivex.android.schedulers.HandlerScheduler}
             *      这个就是 AndroidSchedulers.mainThread() 方法的返回值
             * w：一个 HandlerWorker对象，{@link io.reactivex.android.schedulers.HandlerScheduler.HandlerWorker}
             *      这个对象会提供一个 schedule() 方法，使用这个方法就可以将 线程调度的执行单元扔到
             *      主线程中执行
             */
            Scheduler.Worker w = scheduler.createWorker();

            source.subscribe(new ObserveOnObserver<T>(observer, w, delayError, bufferSize));
        }
    }

    /**
     * Description：通过代理的方式代理原来的观察者对象；可以发现这个对象实现了 Runnable接口，这就意味着这个对象
     * 可以放到指定的线程池中执行；这里以 onNext() 方法做分析
     */
    static final class ObserveOnObserver<T> extends BasicIntQueueDisposable<T>
            implements Observer<T>, Runnable {

        private static final long serialVersionUID = 6576896619930983584L;
        /**
         * 这个是下一个观察者对象的引用
         */
        final Observer<? super T> actual;
        /**
         * 一个 HandlerWorker对象，这个对象可以通俗理解成一个线程，不过这个是主线程，通过 schedule()
         * 方法就可以把视线了 runnable 对象扔个主线程运行，从而实现线程调度的功能
         */
        final Scheduler.Worker worker;
        final boolean delayError;
        final int bufferSize;

        SimpleQueue<T> queue;

        Disposable s;

        Throwable error;
        volatile boolean done;

        volatile boolean cancelled;

        int sourceMode;

        boolean outputFused;

        ObserveOnObserver(Observer<? super T> actual, Scheduler.Worker worker, boolean delayError, int bufferSize) {
            this.actual = actual;
            this.worker = worker;
            this.delayError = delayError;
            this.bufferSize = bufferSize;
        }

        /**
         * 通过查看这个 onSubscribe() 方法可以知道 观察者的 onSubscribe() 方法并没有放到指定的线程中
         * 执行，而是在原线程中执行
         */
        @Override
        public void onSubscribe(Disposable s) {
            if (DisposableHelper.validate(this.s, s)) {
                this.s = s;
                if (s instanceof QueueDisposable) {
                    @SuppressWarnings("unchecked")
                    QueueDisposable<T> qd = (QueueDisposable<T>) s;

                    int m = qd.requestFusion(QueueDisposable.ANY | QueueDisposable.BOUNDARY);

                    if (m == QueueDisposable.SYNC) {
                        sourceMode = m;
                        queue = qd;
                        done = true;
                        actual.onSubscribe(this);
                        schedule();
                        return;
                    }
                    if (m == QueueDisposable.ASYNC) {
                        sourceMode = m;
                        queue = qd;
                        actual.onSubscribe(this);
                        return;
                    }
                }

                queue = new SpscLinkedArrayQueue<T>(bufferSize);

                //
                actual.onSubscribe(this);
            }
        }

        /**
         * 当调用到这个 onNext() 方法后
         *
         * @param t 上一个被观察者对象的事件
         */
        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != QueueDisposable.ASYNC) {
                queue.offer(t);
            }

            // 重点重点：这里调用了这个方法
            schedule();
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            error = t;
            done = true;
            schedule();
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            schedule();
        }

        @Override
        public void dispose() {
            if (!cancelled) {
                cancelled = true;
                s.dispose();
                worker.dispose();
                if (getAndIncrement() == 0) {
                    queue.clear();
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled;
        }


        /**
         * 这个方法会把 ObserveOnObserver这个对象扔给 主线程
         */
        void schedule() {
            if (getAndIncrement() == 0) {
                /**
                 * worker：一个 HandlerWorker对象{@link io.reactivex.android.schedulers.HandlerScheduler.HandlerWorker}
                 * 这一段代码就实现了将 ObserveObserver对象的 run() 方法扔给了主线执行，然后这个方法会
                 * 调用指定的方法，即：onNext() --> schedule()，那么 run() --> 观察者对象的onNext()
                 */
                worker.schedule(this);
            }
        }

        void drainNormal() {
            int missed = 1;

            final SimpleQueue<T> q = queue;
            final Observer<? super T> a = actual;

            for (; ; ) {
                if (checkTerminated(done, q.isEmpty(), a)) {
                    return;
                }

                for (; ; ) {
                    boolean d = done;
                    T v;

                    try {
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        s.dispose();
                        q.clear();
                        a.onError(ex);
                        worker.dispose();
                        return;
                    }
                    boolean empty = v == null;

                    if (checkTerminated(d, empty, a)) {
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    // 在指定的线程池中执行
                    a.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        void drainFused() {
            int missed = 1;

            for (; ; ) {
                if (cancelled) {
                    return;
                }

                boolean d = done;
                Throwable ex = error;

                if (!delayError && d && ex != null) {
                    actual.onError(error);
                    worker.dispose();
                    return;
                }

                actual.onNext(null);

                if (d) {
                    ex = error;
                    if (ex != null) {
                        actual.onError(ex);
                    } else {
                        actual.onComplete();
                    }
                    worker.dispose();
                    return;
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        /**
         * 当这个 ObserveOnObserver对象被扔到线程池的时候，就会执行这个方法
         */
        @Override
        public void run() {
            if (outputFused) {
                drainFused();
            } else {
                drainNormal();
            }
        }

        boolean checkTerminated(boolean d, boolean empty, Observer<? super T> a) {
            if (cancelled) {
                queue.clear();
                return true;
            }
            if (d) {
                Throwable e = error;
                if (delayError) {
                    if (empty) {
                        if (e != null) {
                            a.onError(e);
                        } else {
                            a.onComplete();
                        }
                        worker.dispose();
                        return true;
                    }
                } else {
                    if (e != null) {
                        queue.clear();
                        a.onError(e);
                        worker.dispose();
                        return true;
                    } else if (empty) {
                        a.onComplete();
                        worker.dispose();
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & ASYNC) != 0) {
                outputFused = true;
                return ASYNC;
            }
            return NONE;
        }

        @Nullable
        @Override
        public T poll() throws Exception {
            return queue.poll();
        }

        @Override
        public void clear() {
            queue.clear();
        }

        @Override
        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }
}
