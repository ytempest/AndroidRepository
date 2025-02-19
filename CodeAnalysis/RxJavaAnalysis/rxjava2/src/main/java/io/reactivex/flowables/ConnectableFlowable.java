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

package io.reactivex.flowables;

import io.reactivex.annotations.NonNull;
import org.reactivestreams.Subscriber;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.internal.operators.flowable.*;
import io.reactivex.internal.util.ConnectConsumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * A {@code ConnectableFlowable} resembles an ordinary {@link Flowable}, except that it does not begin
 * emitting items when it is subscribed to, but only when its {@link #connect} method is called. In this way you
 * can wait for all intended {@link Subscriber}s to {@link Flowable#subscribe} to the {@code Flowable}
 * before the {@code Flowable} begins emitting items.
 * <p>
 * <img width="640" height="510" src="https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/publishConnect.png" alt="">
 *
 * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Connectable-Observable-Operators">RxJava Wiki:
 *      Connectable ObservableSource Operators</a>
 * @param <T>
 *          the type of items emitted by the {@code ConnectableFlowable}
 */
public abstract class ConnectableFlowable<T> extends Flowable<T> {

    /**
     * Instructs the {@code ConnectableFlowable} to begin emitting the items from its underlying
     * {@link Flowable} to its {@link Subscriber}s.
     *
     * @param connection
     *          the action that receives the connection subscription before the subscription to source happens
     *          allowing the caller to synchronously disconnect a synchronous source
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">ReactiveX documentation: Connect</a>
     */
    public abstract void connect(@NonNull Consumer<? super Disposable> connection);

    /**
     * Instructs the {@code ConnectableFlowable} to begin emitting the items from its underlying
     * {@link Flowable} to its {@link Subscriber}s.
     * <p>
     * To disconnect from a synchronous source, use the {@link #connect(io.reactivex.functions.Consumer)} method.
     *
     * @return the subscription representing the connection
     * @see <a href="http://reactivex.io/documentation/operators/connect.html">ReactiveX documentation: Connect</a>
     */
    public final Disposable connect() {
        ConnectConsumer cc = new ConnectConsumer();
        connect(cc);
        return cc.disposable;
    }

    /**
     * Returns a {@code Flowable} that stays connected to this {@code ConnectableFlowable} as long as there
     * is at least one subscription to this {@code ConnectableFlowable}.
     *
     * @return a {@link Flowable}
     * @see <a href="http://reactivex.io/documentation/operators/refcount.html">ReactiveX documentation: RefCount</a>
     */
    @NonNull
    public Flowable<T> refCount() {
        return RxJavaPlugins.onAssembly(new FlowableRefCount<T>(this));
    }

    /**
     * Returns a Flowable that automatically connects (at most once) to this ConnectableFlowable
     * when the first Subscriber subscribes.
     * <p>
     * <img width="640" height="392" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.f.png" alt="">
     * <p>
     * The connection happens after the first subscription and happens at most once
     * during the lifetime of the returned Flowable. If this ConnectableFlowable
     * terminates, the connection is never renewed, no matter how Subscribers come
     * and go. Use {@link #refCount()} to renew a connection or dispose an active
     * connection when all {@code Subscriber}s have cancelled their {@code Subscription}s.
     * <p>
     * This overload does not allow disconnecting the connection established via
     * {@link #connect(Consumer)}. Use the {@link #autoConnect(int, Consumer)} overload
     * to gain access to the {@code Disposable} representing the only connection.
     *
     * @return a Flowable that automatically connects to this ConnectableFlowable
     *         when the first Subscriber subscribes
     * @see #refCount()
     * @see #autoConnect(int, Consumer)
     */
    @NonNull
    public Flowable<T> autoConnect() {
        return autoConnect(1);
    }
    /**
     * Returns a Flowable that automatically connects (at most once) to this ConnectableFlowable
     * when the specified number of Subscribers subscribe to it.
     * <p>
     * <img width="640" height="392" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.f.png" alt="">
     * <p>
     * The connection happens after the given number of subscriptions and happens at most once
     * during the lifetime of the returned Flowable. If this ConnectableFlowable
     * terminates, the connection is never renewed, no matter how Subscribers come
     * and go. Use {@link #refCount()} to renew a connection or dispose an active
     * connection when all {@code Subscriber}s have cancelled their {@code Subscription}s.
     * <p>
     * This overload does not allow disconnecting the connection established via
     * {@link #connect(Consumer)}. Use the {@link #autoConnect(int, Consumer)} overload
     * to gain access to the {@code Disposable} representing the only connection.
     *
     * @param numberOfSubscribers the number of subscribers to await before calling connect
     *                            on the ConnectableFlowable. A non-positive value indicates
     *                            an immediate connection.
     * @return a Flowable that automatically connects to this ConnectableFlowable
     *         when the specified number of Subscribers subscribe to it
     */
    @NonNull
    public Flowable<T> autoConnect(int numberOfSubscribers) {
        return autoConnect(numberOfSubscribers, Functions.emptyConsumer());
    }

    /**
     * Returns a Flowable that automatically connects (at most once) to this ConnectableFlowable
     * when the specified number of Subscribers subscribe to it and calls the
     * specified callback with the Subscription associated with the established connection.
     * <p>
     * <img width="640" height="392" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/autoConnect.f.png" alt="">
     * <p>
     * The connection happens after the given number of subscriptions and happens at most once
     * during the lifetime of the returned Flowable. If this ConnectableFlowable
     * terminates, the connection is never renewed, no matter how Subscribers come
     * and go. Use {@link #refCount()} to renew a connection or dispose an active
     * connection when all {@code Subscriber}s have cancelled their {@code Subscription}s.
     *
     * @param numberOfSubscribers the number of subscribers to await before calling connect
     *                            on the ConnectableFlowable. A non-positive value indicates
     *                            an immediate connection.
     * @param connection the callback Consumer that will receive the Subscription representing the
     *                   established connection
     * @return a Flowable that automatically connects to this ConnectableFlowable
     *         when the specified number of Subscribers subscribe to it and calls the
     *         specified callback with the Subscription associated with the established connection
     */
    @NonNull
    public Flowable<T> autoConnect(int numberOfSubscribers, @NonNull Consumer<? super Disposable> connection) {
        if (numberOfSubscribers <= 0) {
            this.connect(connection);
            return RxJavaPlugins.onAssembly(this);
        }
        return RxJavaPlugins.onAssembly(new FlowableAutoConnect<T>(this, numberOfSubscribers, connection));
    }
}
