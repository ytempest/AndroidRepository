/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.internal.operators.mixed;

import java.util.concurrent.atomic.*;

import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.annotations.Experimental;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.*;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.fuseable.SimplePlainQueue;
import io.reactivex.internal.queue.SpscArrayQueue;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.*;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Maps each upstream item into a {@link MaybeSource}, subscribes to them one after the other terminates
 * and relays their success values, optionally delaying any errors till the mainThread and inner sources
 * terminate.
 *
 * @param <T> the upstream element type
 * @param <R> the output element type
 *
 * @since 2.1.11 - experimental
 */
@Experimental
public final class FlowableConcatMapMaybe<T, R> extends Flowable<R> {

    final Flowable<T> source;

    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

    final ErrorMode errorMode;

    final int prefetch;

    public FlowableConcatMapMaybe(Flowable<T> source,
            Function<? super T, ? extends MaybeSource<? extends R>> mapper,
                    ErrorMode errorMode, int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        source.subscribe(new ConcatMapMaybeSubscriber<T, R>(s, mapper, prefetch, errorMode));
    }

    static final class ConcatMapMaybeSubscriber<T, R>
    extends AtomicInteger
    implements FlowableSubscriber<T>, Subscription {

        private static final long serialVersionUID = -9140123220065488293L;

        final Subscriber<? super R> downstream;

        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;

        final int prefetch;

        final AtomicLong requested;

        final AtomicThrowable errors;

        final ConcatMapMaybeObserver<R> inner;

        final SimplePlainQueue<T> queue;

        final ErrorMode errorMode;

        Subscription upstream;

        volatile boolean done;

        volatile boolean cancelled;

        long emitted;

        int consumed;

        R item;

        volatile int state;

        /** No inner MaybeSource is running. */
        static final int STATE_INACTIVE = 0;
        /** An inner MaybeSource is running but there are no results yet. */
        static final int STATE_ACTIVE = 1;
        /** The inner MaybeSource succeeded with a value in {@link #item}. */
        static final int STATE_RESULT_VALUE = 2;

        ConcatMapMaybeSubscriber(Subscriber<? super R> downstream,
                Function<? super T, ? extends MaybeSource<? extends R>> mapper,
                        int prefetch, ErrorMode errorMode) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.prefetch = prefetch;
            this.errorMode = errorMode;
            this.requested = new AtomicLong();
            this.errors = new AtomicThrowable();
            this.inner = new ConcatMapMaybeObserver<R>(this);
            this.queue = new SpscArrayQueue<T>(prefetch);
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(upstream, s)) {
                upstream = s;
                downstream.onSubscribe(this);
                s.request(prefetch);
            }
        }

        @Override
        public void onNext(T t) {
            if (!queue.offer(t)) {
                upstream.cancel();
                onError(new MissingBackpressureException("queue full?!"));
                return;
            }
            drain();
        }

        @Override
        public void onError(Throwable t) {
            if (errors.addThrowable(t)) {
                if (errorMode == ErrorMode.IMMEDIATE) {
                    inner.dispose();
                }
                done = true;
                drain();
            } else {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        @Override
        public void request(long n) {
            BackpressureHelper.add(requested, n);
            drain();
        }

        @Override
        public void cancel() {
            cancelled = true;
            upstream.cancel();
            inner.dispose();
            if (getAndIncrement() == 0) {
                queue.clear();
                item = null;
            }
        }

        void innerSuccess(R item) {
            this.item = item;
            this.state = STATE_RESULT_VALUE;
            drain();
        }

        void innerComplete() {
            this.state = STATE_INACTIVE;
            drain();
        }

        void innerError(Throwable ex) {
            if (errors.addThrowable(ex)) {
                if (errorMode != ErrorMode.END) {
                    upstream.cancel();
                }
                this.state = STATE_INACTIVE;
                drain();
            } else {
                RxJavaPlugins.onError(ex);
            }
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }

            int missed = 1;
            Subscriber<? super R> downstream = this.downstream;
            ErrorMode errorMode = this.errorMode;
            SimplePlainQueue<T> queue = this.queue;
            AtomicThrowable errors = this.errors;
            AtomicLong requested = this.requested;
            int limit = prefetch - (prefetch >> 1);

            for (;;) {

                for (;;) {
                    if (cancelled) {
                        queue.clear();
                        item = null;
                    }

                    int s = state;

                    if (errors.get() != null) {
                        if (errorMode == ErrorMode.IMMEDIATE
                                || (errorMode == ErrorMode.BOUNDARY && s == STATE_INACTIVE)) {
                            queue.clear();
                            item = null;
                            Throwable ex = errors.terminate();
                            downstream.onError(ex);
                            return;
                        }
                    }

                    if (s == STATE_INACTIVE) {
                        boolean d = done;
                        T v = queue.poll();
                        boolean empty = v == null;

                        if (d && empty) {
                            Throwable ex = errors.terminate();
                            if (ex == null) {
                                downstream.onComplete();
                            } else {
                                downstream.onError(ex);
                            }
                            return;
                        }

                        if (empty) {
                            break;
                        }

                        int c = consumed + 1;
                        if (c == limit) {
                            consumed = 0;
                            upstream.request(limit);
                        } else {
                            consumed = c;
                        }

                        MaybeSource<? extends R> ms;

                        try {
                            ms = ObjectHelper.requireNonNull(mapper.apply(v), "The mapper returned a null MaybeSource");
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            upstream.cancel();
                            queue.clear();
                            errors.addThrowable(ex);
                            ex = errors.terminate();
                            downstream.onError(ex);
                            return;
                        }

                        state = STATE_ACTIVE;
                        ms.subscribe(inner);
                        break;
                    } else if (s == STATE_RESULT_VALUE) {
                        long e = emitted;
                        if (e != requested.get()) {
                            R w = item;
                            item = null;

                            downstream.onNext(w);

                            emitted = e + 1;
                            state = STATE_INACTIVE;
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                missed = addAndGet(-missed);
                if (missed == 0) {
                    break;
                }
            }
        }

        static final class ConcatMapMaybeObserver<R>
        extends AtomicReference<Disposable>
        implements MaybeObserver<R> {

            private static final long serialVersionUID = -3051469169682093892L;

            final ConcatMapMaybeSubscriber<?, R> parent;

            ConcatMapMaybeObserver(ConcatMapMaybeSubscriber<?, R> parent) {
                this.parent = parent;
            }

            @Override
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(this, d);
            }

            @Override
            public void onSuccess(R t) {
                parent.innerSuccess(t);
            }

            @Override
            public void onError(Throwable e) {
                parent.innerError(e);
            }

            @Override
            public void onComplete() {
                parent.innerComplete();
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
