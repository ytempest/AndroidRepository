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

package io.reactivex.internal.operators.flowable;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Scheduler;
import io.reactivex.Scheduler.Worker;
import io.reactivex.annotations.Nullable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.exceptions.MissingBackpressureException;
import io.reactivex.internal.fuseable.ConditionalSubscriber;
import io.reactivex.internal.fuseable.QueueSubscription;
import io.reactivex.internal.fuseable.SimpleQueue;
import io.reactivex.internal.queue.SpscArrayQueue;
import io.reactivex.internal.subscriptions.BasicIntQueueSubscription;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Description：这个类实现将下游的逻辑放在指定的线程中执行
 *
 * @param <T> 事件类型
 */
public final class FlowableObserveOn<T> extends AbstractFlowableWithUpstream<T, T> {
    final Scheduler scheduler;

    final boolean delayError;

    // 这个就是用来创建指定大小的队列，由于上游发送事件速度和下游处理事件速度不一致，所以这个队列
    // 用于存储上游发送过来的事件
    final int prefetch;


    public FlowableObserveOn(Flowable<T> source, Scheduler scheduler, boolean delayError, int prefetch) {
        super(source);
        this.scheduler = scheduler;
        this.delayError = delayError;
        this.prefetch = prefetch;
    }

    @Override
    public void subscribeActual(Subscriber<? super T> s) {
        Worker worker = scheduler.createWorker();

        if (s instanceof ConditionalSubscriber) {
            source.subscribe(new ObserveOnConditionalSubscriber<T>(
                    (ConditionalSubscriber<? super T>) s, worker, delayError, prefetch));
        } else {
            // 一般情况下都会走到这里
            source.subscribe(new ObserveOnSubscriber<T>(s, worker, delayError, prefetch));
        }
    }

    abstract static class BaseObserveOnSubscriber<T> extends BasicIntQueueSubscription<T>
            implements FlowableSubscriber<T>, Runnable {
        private static final long serialVersionUID = -8241002408341274697L;

        final Worker worker;

        final boolean delayError;

        final int prefetch;

        final int limit;

        // 这个值表示了下游的处理能力值
        final AtomicLong requested;

        Subscription s;

        SimpleQueue<T> queue;

        volatile boolean cancelled;

        volatile boolean done;

        Throwable error;

        int sourceMode;

        long produced;

        boolean outputFused;

        BaseObserveOnSubscriber(Worker worker, boolean delayError, int prefetch) {
            this.worker = worker;
            this.delayError = delayError;
            this.prefetch = prefetch;
            this.requested = new AtomicLong();
            this.limit = prefetch - (prefetch >> 2);
        }

        @Override
        public final void onNext(T t) {
            if (done) {
                return;
            }
            if (sourceMode == ASYNC) {
                trySchedule();
                return;
            }

            // queue.offer(t)：将事件存储到 queue当中，然后会在 runAsync()方法中取出并使用
            if (!queue.offer(t)) {
                s.cancel();
                error = new MissingBackpressureException("Queue is full?!");
                done = true;
            }

            // 一般情况下都会走到这里的
            trySchedule();
        }

        @Override
        public final void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            error = t;
            done = true;
            trySchedule();
        }

        @Override
        public final void onComplete() {
            if (!done) {
                done = true;
                trySchedule();
            }
        }

        @Override
        public final void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                // 将下游新的处理能力值加上旧的处理能力值，并赋给 requested
                BackpressureHelper.add(requested, n);
                trySchedule();
            }
        }

        @Override
        public final void cancel() {
            if (cancelled) {
                return;
            }

            cancelled = true;
            s.cancel();
            worker.dispose();

            if (getAndIncrement() == 0) {
                queue.clear();
            }
        }

        final void trySchedule() {
            if (getAndIncrement() != 0) {
                return;
            }
            worker.schedule(this);
        }

        @Override
        public final void run() {
            if (outputFused) {
                runBackfused();
            } else if (sourceMode == SYNC) {
                runSync();
            } else {
                // 一般情况下都是走到这里，这个由 BaseObserveOnSubscriber的子类 ObserveOnSubscriber
                // 进行了实现
                runAsync();
            }
        }

        abstract void runBackfused();

        abstract void runSync();

        abstract void runAsync();

        final boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a) {
            if (cancelled) {
                clear();
                return true;
            }
            if (d) {
                if (delayError) {
                    if (empty) {
                        Throwable e = error;
                        if (e != null) {
                            a.onError(e);
                        } else {
                            a.onComplete();
                        }
                        worker.dispose();
                        return true;
                    }
                } else {
                    Throwable e = error;
                    if (e != null) {
                        clear();
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
        public final int requestFusion(int requestedMode) {
            if ((requestedMode & ASYNC) != 0) {
                outputFused = true;
                return ASYNC;
            }
            return NONE;
        }

        @Override
        public final void clear() {
            queue.clear();
        }

        @Override
        public final boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    /**
     * Description：这个类是下游（即Subscriber）的代理类，这个类相当于上游和下游之间的一个中游
     */
    static final class ObserveOnSubscriber<T> extends BaseObserveOnSubscriber<T>
            implements FlowableSubscriber<T> {

        private static final long serialVersionUID = -4547113800637756442L;

        final Subscriber<? super T> actual;

        ObserveOnSubscriber(Subscriber<? super T> actual, Worker worker, boolean delayError, int prefetch) {
            super(worker, delayError, prefetch);
            this.actual = actual;
        }

        /**
         * 我们知道下游在 onSubscribe()方法中使用上游发送过来的Subscription对象，通过调用这个对象的
         * request()方法就实现了告知上游——下游的处理能力，所以重点在这个 onSubscribe()方法
         *
         * @param s 上游传递过来的 Subscription，通过这个对象可以告知上游——下游的处理能力
         */
        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.s, s)) {
                this.s = s;

                if (s instanceof QueueSubscription) {
                    // 一般情况下都不会这个逻辑
                    @SuppressWarnings("unchecked")
                    QueueSubscription<T> f = (QueueSubscription<T>) s;

                    int m = f.requestFusion(ANY | BOUNDARY);

                    if (m == SYNC) {
                        sourceMode = SYNC;
                        queue = f;
                        done = true;

                        actual.onSubscribe(this);
                        return;
                    } else if (m == ASYNC) {
                        sourceMode = ASYNC;
                        queue = f;

                        actual.onSubscribe(this);

                        s.request(prefetch);

                        return;
                    }
                }

                // 创建一个长度为 prefetch(128) 的队列，用于存储上游发送过来的事件
                // 既然是会存储上游发送过来的事件，那么肯定会在 onNext()方法中使用到这个 queue
                queue = new SpscArrayQueue<T>(prefetch);

                // 接着把这个代理类（中游）传递给下游，会转型为 Subscription
                // 由此可知，当使用 observeOn()指定线程或，下游的 onSubscribe()方法接收到的Subscription
                // 不再是上游的 Subscription，而是中游的 Subscription
                actual.onSubscribe(this);

                // 设置中游事件存储队列的大小，也就是告知上游发送的事件只要不超过128就都可以存储起来
                s.request(prefetch);
            }
        }

        @Override
        void runSync() {
            int missed = 1;

            final Subscriber<? super T> a = actual;
            final SimpleQueue<T> q = queue;

            long e = produced;

            for (; ; ) {

                long r = requested.get();

                while (e != r) {
                    T v;

                    try {
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        s.cancel();
                        a.onError(ex);
                        worker.dispose();
                        return;
                    }

                    if (cancelled) {
                        return;
                    }
                    if (v == null) {
                        a.onComplete();
                        worker.dispose();
                        return;
                    }

                    a.onNext(v);

                    e++;
                }

                if (cancelled) {
                    return;
                }

                if (q.isEmpty()) {
                    a.onComplete();
                    worker.dispose();
                    return;
                }

                int w = get();
                if (missed == w) {
                    produced = e;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }

        /**
         * 线程调度之后，Runnable的 run()方法最终会调用到这个方法中
         */
        @Override
        void runAsync() {
            int missed = 1;
            // 获取到下游对象
            final Subscriber<? super T> a = actual;
            // 获取存储事件的队列的引用
            final SimpleQueue<T> q = queue;

            // produced：表示已经下游已经处理的事件数量
            // 首次进来produced为0，因为下游还没有处理事件，那么e=0
            long e = produced;

            for (; ; ) {
                // 获取下游的处理能力值
                long r = requested.get();

                // 如果下游已经处理的事件数量不等于下游的事件处理能力，就表示下游还可以处理事件
                while (e != r) {
                    boolean d = done;
                    T v;
                    try {
                        // 获取事件存储队列的队首事件
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        s.cancel();
                        q.clear();
                        a.onError(ex);
                        worker.dispose();
                        return;
                    }

                    boolean empty = v == null;

                    // 检测该事件是否已经被处理了
                    if (checkTerminated(d, empty, a)) {
                        return;
                    }

                    // 如果事件为空就break
                    if (empty) {
                        break;
                    }

                    // 最后没什么问题的话，就把事件发送给 下游
                    a.onNext(v);

                    // 将下游已经处理的事件数量加一
                    e++;

                    // limit：这个值会在 ObserveOnSubscriber的构造方法中初始化（limit = prefetch - (prefetch >> 2)）
                    // 如果说下游处理事件的数量已经等于 96，那么就调用 s.request(e)通知上游，我下游的处理
                    // 能力值要再加 e；也就是说，下游每处理掉96个事件后，那么上游的 requested()的值就会更新
                    if (e == limit) {
                        // 只要下游的事件处理能力值不是 Long.MAX_VALUE
                        if (r != Long.MAX_VALUE) {
                            // addAndGet(-e):将 -e加到requested中，然后返回这个requested
                            // 因为requested=96，e=96，所以r=0
                            r = requested.addAndGet(-e);
                        }
                        // s：上游传递过来的 Subscription对象
                        // 使用 request()方法再添加中游的事件处理能力值，如果此时变成33了，那么再加上96
                        // 所以，就变成128了
                        s.request(e);
                        e = 0L;
                    }
                }

                if (e == r && checkTerminated(done, q.isEmpty(), a)) {
                    return;
                }

                // 获取下游的处理能力值
                int w = get();
                if (missed == w) {
                    produced = e;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }

        @Override
        void runBackfused() {
            int missed = 1;

            for (; ; ) {

                if (cancelled) {
                    return;
                }

                boolean d = done;

                actual.onNext(null);

                if (d) {
                    Throwable e = error;
                    if (e != null) {
                        actual.onError(e);
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
         * 这个方法用于获取 事件存储队列中的事件
         *
         * @return 队列的队首事件
         */
        @Nullable
        @Override
        public T poll() throws Exception {
            T v = queue.poll();
            if (v != null && sourceMode != SYNC) {
                long p = produced + 1;
                if (p == limit) {
                    produced = 0;
                    s.request(p);
                } else {
                    produced = p;
                }
            }
            return v;
        }

    }

    static final class ObserveOnConditionalSubscriber<T>
            extends BaseObserveOnSubscriber<T> {

        private static final long serialVersionUID = 644624475404284533L;

        final ConditionalSubscriber<? super T> actual;

        long consumed;

        ObserveOnConditionalSubscriber(
                ConditionalSubscriber<? super T> actual,
                Worker worker,
                boolean delayError,
                int prefetch) {
            super(worker, delayError, prefetch);
            this.actual = actual;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.s, s)) {
                this.s = s;

                if (s instanceof QueueSubscription) {
                    @SuppressWarnings("unchecked")
                    QueueSubscription<T> f = (QueueSubscription<T>) s;

                    int m = f.requestFusion(ANY | BOUNDARY);

                    if (m == SYNC) {
                        sourceMode = SYNC;
                        queue = f;
                        done = true;

                        actual.onSubscribe(this);
                        return;
                    } else if (m == ASYNC) {
                        sourceMode = ASYNC;
                        queue = f;

                        actual.onSubscribe(this);

                        s.request(prefetch);

                        return;
                    }
                }

                queue = new SpscArrayQueue<T>(prefetch);

                actual.onSubscribe(this);

                s.request(prefetch);
            }
        }

        @Override
        void runSync() {
            int missed = 1;

            final ConditionalSubscriber<? super T> a = actual;
            final SimpleQueue<T> q = queue;

            long e = produced;

            for (; ; ) {

                long r = requested.get();

                while (e != r) {
                    T v;
                    try {
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        s.cancel();
                        a.onError(ex);
                        worker.dispose();
                        return;
                    }

                    if (cancelled) {
                        return;
                    }
                    if (v == null) {
                        a.onComplete();
                        worker.dispose();
                        return;
                    }

                    if (a.tryOnNext(v)) {
                        e++;
                    }
                }

                if (cancelled) {
                    return;
                }

                if (q.isEmpty()) {
                    a.onComplete();
                    worker.dispose();
                    return;
                }

                int w = get();
                if (missed == w) {
                    produced = e;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }
        }

        @Override
        void runAsync() {
            int missed = 1;

            final ConditionalSubscriber<? super T> a = actual;
            final SimpleQueue<T> q = queue;

            long emitted = produced;
            long polled = consumed;

            for (; ; ) {

                long r = requested.get();

                while (emitted != r) {
                    boolean d = done;
                    T v;
                    try {
                        v = q.poll();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);

                        s.cancel();
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

                    if (a.tryOnNext(v)) {
                        emitted++;
                    }

                    polled++;

                    if (polled == limit) {
                        s.request(polled);
                        polled = 0L;
                    }
                }

                if (emitted == r && checkTerminated(done, q.isEmpty(), a)) {
                    return;
                }

                int w = get();
                if (missed == w) {
                    produced = emitted;
                    consumed = polled;
                    missed = addAndGet(-missed);
                    if (missed == 0) {
                        break;
                    }
                } else {
                    missed = w;
                }
            }

        }

        @Override
        void runBackfused() {
            int missed = 1;

            for (; ; ) {

                if (cancelled) {
                    return;
                }

                boolean d = done;

                actual.onNext(null);

                if (d) {
                    Throwable e = error;
                    if (e != null) {
                        actual.onError(e);
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

        @Nullable
        @Override
        public T poll() throws Exception {
            T v = queue.poll();
            if (v != null && sourceMode != SYNC) {
                long p = consumed + 1;
                if (p == limit) {
                    consumed = 0;
                    s.request(p);
                } else {
                    consumed = p;
                }
            }
            return v;
        }
    }
}
