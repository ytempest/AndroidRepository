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

import java.util.concurrent.atomic.*;

import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.*;
import io.reactivex.functions.Cancellable;
import io.reactivex.internal.disposables.*;
import io.reactivex.internal.fuseable.SimplePlainQueue;
import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.*;
import io.reactivex.plugins.RxJavaPlugins;


public final class FlowableCreate<T> extends Flowable<T> {

    final FlowableOnSubscribe<T> source;

    final BackpressureStrategy backpressure;

    public FlowableCreate(FlowableOnSubscribe<T> source, BackpressureStrategy backpressure) {
        this.source = source;
        this.backpressure = backpressure;
    }

    /**
     * 当执行到这里的时候表示事件流到了 FlowableCreate中
     *
     * @param t 下游对象，相当于使用 Observable时的 Observer
     */
    @Override
    public void subscribeActual(Subscriber<? super T> t) {
        BaseEmitter<T> emitter;

        // 根据不同的背压模式初始化不同的 Emitter
        switch (backpressure) {
            case MISSING: {
                emitter = new MissingEmitter<T>(t);
                break;
            }
            case ERROR: {
                emitter = new ErrorAsyncEmitter<T>(t);
                break;
            }
            case DROP: {
                emitter = new DropAsyncEmitter<T>(t);
                break;
            }
            case LATEST: {
                emitter = new LatestAsyncEmitter<T>(t);
                break;
            }
            default: {
                emitter = new BufferAsyncEmitter<T>(t, bufferSize());
                break;
            }
        }

        // 首先调用 下游对象的 onSubscribe()方法，并将 emitter传递过去，让下游对象使用
        // emitter.request()方法告知上游，下游的处理能力
        t.onSubscribe(emitter);
        try {
            // 把逻辑交给用户使用 Flowable.create()创建的 FlowableOnSubscribe上游对象执行
            source.subscribe(emitter);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            emitter.onError(ex);
        }
    }

    /**
     * Serializes calls to onNext, onError and onComplete.
     *
     * @param <T> the value type
     */
    static final class SerializedEmitter<T>
            extends AtomicInteger
            implements FlowableEmitter<T> {

        private static final long serialVersionUID = 4883307006032401862L;

        final BaseEmitter<T> emitter;

        final AtomicThrowable error;

        final SimplePlainQueue<T> queue;

        volatile boolean done;

        SerializedEmitter(BaseEmitter<T> emitter) {
            this.emitter = emitter;
            this.error = new AtomicThrowable();
            this.queue = new SpscLinkedArrayQueue<T>(16);
        }

        @Override
        public void onNext(T t) {
            if (emitter.isCancelled() || done) {
                return;
            }
            if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }
            if (get() == 0 && compareAndSet(0, 1)) {
                emitter.onNext(t);
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimplePlainQueue<T> q = queue;
                synchronized (q) {
                    q.offer(t);
                }
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override
        public void onError(Throwable t) {
            if (!tryOnError(t)) {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public boolean tryOnError(Throwable t) {
            if (emitter.isCancelled() || done) {
                return false;
            }
            if (t == null) {
                t = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (error.addThrowable(t)) {
                done = true;
                drain();
                return true;
            }
            return false;
        }

        @Override
        public void onComplete() {
            if (emitter.isCancelled() || done) {
                return;
            }
            done = true;
            drain();
        }

        void drain() {
            if (getAndIncrement() == 0) {
                drainLoop();
            }
        }

        void drainLoop() {
            BaseEmitter<T> e = emitter;
            SimplePlainQueue<T> q = queue;
            AtomicThrowable error = this.error;
            int missed = 1;
            for (; ; ) {

                for (; ; ) {
                    if (e.isCancelled()) {
                        q.clear();
                        return;
                    }

                    if (error.get() != null) {
                        q.clear();
                        e.onError(error.terminate());
                        return;
                    }

                    boolean d = done;

                    T v = q.poll();

                    boolean empty = v == null;

                    if (d && empty) {
                        e.onComplete();
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    e.onNext(v);
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        @Override
        public void setDisposable(Disposable s) {
            emitter.setDisposable(s);
        }

        @Override
        public void setCancellable(Cancellable c) {
            emitter.setCancellable(c);
        }

        @Override
        public long requested() {
            return emitter.requested();
        }

        @Override
        public boolean isCancelled() {
            return emitter.isCancelled();
        }

        @Override
        public FlowableEmitter<T> serialize() {
            return this;
        }
    }

    /**
     * 这个是基层的Emitter发射器，这个发射器会通过 onSubscribe()方法传递给 下游对象（即：Subscriber），
     * 然后下游对象会调用 request()方法告知上游，下游能处理多少事件
     *
     * @param <T> 事件类型
     */
    abstract static class BaseEmitter<T> extends AtomicLong
            implements FlowableEmitter<T>, Subscription {
        private static final long serialVersionUID = 7326289992464377023L;

        final Subscriber<? super T> actual;

        final SequentialDisposable serial;

        BaseEmitter(Subscriber<? super T> actual) {
            this.actual = actual;
            this.serial = new SequentialDisposable();
        }

        @Override
        public void onComplete() {
            complete();
        }

        protected void complete() {
            if (isCancelled()) {
                return;
            }
            try {
                actual.onComplete();
            } finally {
                serial.dispose();
            }
        }

        @Override
        public final void onError(Throwable e) {
            if (!tryOnError(e)) {
                RxJavaPlugins.onError(e);
            }
        }

        @Override
        public boolean tryOnError(Throwable e) {
            return error(e);
        }

        protected boolean error(Throwable e) {
            if (e == null) {
                e = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }
            if (isCancelled()) {
                return false;
            }
            try {
                actual.onError(e);
            } finally {
                serial.dispose();
            }
            return true;
        }

        @Override
        public final void cancel() {
            serial.dispose();
            onUnsubscribed();
        }

        void onUnsubscribed() {
            // default is no-op
        }

        @Override
        public final boolean isCancelled() {
            return serial.isDisposed();
        }

        /**
         * 这个方法会由下游对象（即：Subscriber）调用
         *
         * @param n 处理能力值
         */
        @Override
        public final void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this, n);
                onRequested();
            }
        }

        void onRequested() {
            // default is no-op
        }

        @Override
        public final void setDisposable(Disposable s) {
            serial.update(s);
        }

        @Override
        public final void setCancellable(Cancellable c) {
            setDisposable(new CancellableDisposable(c));
        }

        /**
         * 获取下游对象的处理能力，如果下游对象没有在 onSubscribe()方法中设置处理能力值，那么就
         * 默认处理能力为0
         *
         * @return 0 或者 下游对象设置的处理能力值
         */
        @Override
        public final long requested() {
            return get();
        }

        @Override
        public final FlowableEmitter<T> serialize() {
            return new SerializedEmitter<T>(this);
        }
    }

    static final class MissingEmitter<T> extends BaseEmitter<T> {

        private static final long serialVersionUID = 3776720187248809713L;

        MissingEmitter(Subscriber<? super T> actual) {
            super(actual);
        }

        @Override
        public void onNext(T t) {
            if (isCancelled()) {
                return;
            }

            if (t != null) {
                actual.onNext(t);
            } else {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }

            for (; ; ) {
                long r = get();
                if (r == 0L || compareAndSet(r, r - 1)) {
                    return;
                }
            }
        }

    }

    /**
     * Description：这个是定义了当出现下游没有处理能力的时候该如何处理的规范（定义了一个抽象的
     * onOverflow()方法），让子类实现这个方法进行对这个问题的处理；在这个类中的 onNext()方法会
     * 检测下游是否有处理能力，如果有就把上游发送的事件发送给下游，否则调用 onOverflow()方法
     */
    abstract static class NoOverflowBaseAsyncEmitter<T> extends BaseEmitter<T> {

        private static final long serialVersionUID = 4127754106204442833L;

        NoOverflowBaseAsyncEmitter(Subscriber<? super T> actual) {
            super(actual);
        }

        /**
         * 在调用下游的 onNext()方法前判断下游是否有处理能力，如果处理能力值为0 就调用onOverflow()
         * 方法处理这个问题
         *
         * @param t 上游发送的事件，也就是说这个检测（检测下游是否有处理能力）发生在上游发送事件之后
         */
        @Override
        public final void onNext(T t) {
            if (isCancelled()) {
                return;
            }

            if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }

            // get()方法获取的就是下游的处理能力值，requested()内部就是用 get()方法获取的
            if (get() != 0) {
                // actual：这个是用户定义的 Subscriber下游对象
                actual.onNext(t);
                // 每使用 onNext()方法发送一个事件，那么就将下游的处理能力值减一
                BackpressureHelper.produced(this, 1);
            } else {
                onOverflow();
            }
        }

        /**
         * 这个方法会在下游对象（即：Subscriber）没有处理能力的时候调用，即：requested()返回值为0；
         * 这个方法定义当下游对象没有处理能力的时候，Emitter应该如何处理
         */
        abstract void onOverflow();
    }

    /**
     * Description：这是一个背压模式为 DROP的发射器，这个发射器解决上游发送事件和下游处理事件速度不一致
     * 的方法是：当存储事件的队列已经满的时候，那么就会丢弃上游最新发送过来的事件
     */
    static final class DropAsyncEmitter<T> extends NoOverflowBaseAsyncEmitter<T> {


        private static final long serialVersionUID = 8360058422307496563L;

        DropAsyncEmitter(Subscriber<? super T> actual) {
            super(actual);
        }

        /**
         * 当出现上游和下游的速度不一致的时候，什么也不用做，也就是丢弃上游发送过来的最新的事件
         */
        @Override
        void onOverflow() {
            // nothing to do
        }

    }

    /**
     * Description：实现了 onOverflow()方法，当出现下游没有处理能力的时候直接抛出一个
     * MissingBackpressureException异常
     */
    static final class ErrorAsyncEmitter<T> extends NoOverflowBaseAsyncEmitter<T> {

        private static final long serialVersionUID = 338953216916120960L;

        ErrorAsyncEmitter(Subscriber<? super T> actual) {
            super(actual);
        }

        /**
         * 当下游没有处理能力的时候直接抛出一个 MissingBackpressureException异常
         */
        @Override
        void onOverflow() {
            onError(new MissingBackpressureException("create: could not emit value due to lack of requests"));
        }

    }


    /**
     * Description：这是一个背压模式为 BUFFER的发射器，这个发射器解决上游发送事件和下游处理事件速度不
     * 一致的方法是扩充存储事件的队列；这个类里面有一个初始长度为128的队列，用于存储事件当存储事件的数
     * 量等于 127*n，就会对队列进行扩容
     */
    static final class BufferAsyncEmitter<T> extends BaseEmitter<T> {

        private static final long serialVersionUID = 2427151001689639875L;

        final SpscLinkedArrayQueue<T> queue;

        Throwable error;
        volatile boolean done;

        final AtomicInteger wip;

        BufferAsyncEmitter(Subscriber<? super T> actual, int capacityHint) {
            super(actual);
            // 创建一个初始长度饿 SpscLinkedArrayQueue，当队列的长度等于 127*n，就会对队列进行扩容
            this.queue = new SpscLinkedArrayQueue<T>(capacityHint);
            this.wip = new AtomicInteger();
        }

        @Override
        public void onNext(T t) {
            if (done || isCancelled()) {
                return;
            }

            if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }
            queue.offer(t);
            drain();
        }

        @Override
        public boolean tryOnError(Throwable e) {
            if (done || isCancelled()) {
                return false;
            }

            if (e == null) {
                e = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
            }

            error = e;
            done = true;
            drain();
            return true;
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        @Override
        void onRequested() {
            drain();
        }

        @Override
        void onUnsubscribed() {
            if (wip.getAndIncrement() == 0) {
                queue.clear();
            }
        }

        void drain() {
            if (wip.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            final Subscriber<? super T> a = actual;
            final SpscLinkedArrayQueue<T> q = queue;

            for (; ; ) {
                // 获取下游的处理能力值
                long r = get();
                // 这个表示下游已经处理的事件数量
                long e = 0L;

                // 如果处理的事件数还没达到下游的处理能力值
                while (e != r) {
                    if (isCancelled()) {
                        q.clear();
                        return;
                    }

                    boolean d = done;

                    // 获取事件存储队列的队首事件
                    T o = q.poll();

                    boolean empty = o == null;

                    if (d && empty) {
                        Throwable ex = error;
                        if (ex != null) {
                            error(ex);
                        } else {
                            complete();
                        }
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    // 如果没有问题，就将事件传递给下游
                    a.onNext(o);

                    // 已经处理的事件数量加一
                    e++;
                }

                // 执行到这里，表示传递给下游的事件数量达到下游的处理能力值

                if (e == r) {
                    if (isCancelled()) {
                        q.clear();
                        return;
                    }

                    boolean d = done;

                    boolean empty = q.isEmpty();

                    if (d && empty) {
                        Throwable ex = error;
                        if (ex != null) {
                            error(ex);
                        } else {
                            complete();
                        }
                        return;
                    }
                }

                if (e != 0) {
                    BackpressureHelper.produced(this, e);
                }

                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

    static final class LatestAsyncEmitter<T> extends BaseEmitter<T> {


        private static final long serialVersionUID = 4023437720691792495L;

        final AtomicReference<T> queue;

        Throwable error;
        volatile boolean done;

        final AtomicInteger wip;

        LatestAsyncEmitter(Subscriber<? super T> actual) {
            super(actual);
            this.queue = new AtomicReference<T>();
            this.wip = new AtomicInteger();
        }

        @Override
        public void onNext(T t) {
            if (done || isCancelled()) {
                return;
            }

            if (t == null) {
                onError(new NullPointerException("onNext called with null. Null values are generally not allowed in 2.x operators and sources."));
                return;
            }
            queue.set(t);
            drain();
        }

        @Override
        public boolean tryOnError(Throwable e) {
            if (done || isCancelled()) {
                return false;
            }
            if (e == null) {
                onError(new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources."));
            }
            error = e;
            done = true;
            drain();
            return true;
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        @Override
        void onRequested() {
            drain();
        }

        @Override
        void onUnsubscribed() {
            if (wip.getAndIncrement() == 0) {
                queue.lazySet(null);
            }
        }

        void drain() {
            if (wip.getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            final Subscriber<? super T> a = actual;
            final AtomicReference<T> q = queue;

            for (; ; ) {
                long r = get();
                long e = 0L;

                while (e != r) {
                    if (isCancelled()) {
                        q.lazySet(null);
                        return;
                    }

                    boolean d = done;

                    T o = q.getAndSet(null);

                    boolean empty = o == null;

                    if (d && empty) {
                        Throwable ex = error;
                        if (ex != null) {
                            error(ex);
                        } else {
                            complete();
                        }
                        return;
                    }

                    if (empty) {
                        break;
                    }

                    a.onNext(o);

                    e++;
                }

                if (e == r) {
                    if (isCancelled()) {
                        q.lazySet(null);
                        return;
                    }

                    boolean d = done;

                    boolean empty = q.get() == null;

                    if (d && empty) {
                        Throwable ex = error;
                        if (ex != null) {
                            error(ex);
                        } else {
                            complete();
                        }
                        return;
                    }
                }

                if (e != 0) {
                    BackpressureHelper.produced(this, e);
                }

                missed = wip.addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }
    }

}
