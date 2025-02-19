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

package io.reactivex.observers;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.*;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.util.EndConsumerHelper;

/**
 * An abstract {@link SingleObserver} that allows asynchronous cancellation of its subscription
 * and the associated resources.
 *
 * <p>All pre-implemented final methods are thread-safe.
 *
 * <p>Override the protected {@link #onStart()} to perform initialization when this
 * {@code ResourceSingleObserver} is subscribed to a source.
 *
 * <p>Use the public {@link #dispose()} method to dispose the sequence externally and release
 * all resources.
 *
 * <p>To release the associated resources, one has to call {@link #dispose()}
 * in {@code onSuccess()} and {@code onError()} explicitly.
 *
 * <p>Use {@link #add(Disposable)} to associate resources (as {@link io.reactivex.disposables.Disposable Disposable}s)
 * with this {@code ResourceSingleObserver} that will be cleaned up when {@link #dispose()} is called.
 * Removing previously associated resources is not possible but one can create a
 * {@link io.reactivex.disposables.CompositeDisposable CompositeDisposable}, associate it with this
 * {@code ResourceSingleObserver} and then add/remove resources to/from the {@code CompositeDisposable}
 * freely.
 *
 * <p>Like all other consumers, {@code ResourceSingleObserver} can be subscribed only once.
 * Any subsequent attempt to subscribe it to a new source will yield an
 * {@link IllegalStateException} with message {@code "It is not allowed to subscribe with a(n) <class name> multiple times."}.
 *
 * <p>Implementation of {@link #onStart()}, {@link #onSuccess(Object)} and {@link #onError(Throwable)}
 * are not allowed to throw any unchecked exceptions.
 *
 * <p>Example<pre><code>
 * Disposable d =
 *     Single.subscriber(1).delay(1, TimeUnit.SECONDS)
 *     .subscribeWith(new ResourceSingleObserver&lt;Integer&gt;() {
 *         &#64;Override public void onStart() {
 *             add(Schedulers.single()
 *                 .scheduleDirect(() -&gt; System.out.println("Time!"),
 *                     2, TimeUnit.SECONDS));
 *         }
 *         &#64;Override public void onSuccess(Integer t) {
 *             System.out.println(t);
 *             dispose();
 *         }
 *         &#64;Override public void onError(Throwable t) {
 *             t.printStackTrace();
 *             dispose();
 *         }
 *     });
 * // ...
 * d.dispose();
 * </code></pre>
 *
 * @param <T> the value type
 */
public abstract class ResourceSingleObserver<T> implements SingleObserver<T>, Disposable {
    /** The active subscription. */
    private final AtomicReference<Disposable> s = new AtomicReference<Disposable>();

    /** The resource composite, can never be null. */
    private final ListCompositeDisposable resources = new ListCompositeDisposable();

    /**
     * Adds a resource to this ResourceObserver.
     *
     * @param resource the resource to add
     *
     * @throws NullPointerException if resource is null
     */
    public final void add(@NonNull Disposable resource) {
        ObjectHelper.requireNonNull(resource, "resource is null");
        resources.add(resource);
    }

    @Override
    public final void onSubscribe(@NonNull Disposable s) {
        if (EndConsumerHelper.setOnce(this.s, s, getClass())) {
            onStart();
        }
    }

    /**
     * Called once the upstream sets a Subscription on this ResourceObserver.
     *
     * <p>You can perform initialization at this moment. The default
     * implementation does nothing.
     */
    protected void onStart() {
    }

    /**
     * Cancels the mainThread disposable (if any) and disposes the resources associated with
     * this ResourceObserver (if any).
     *
     * <p>This method can be called before the upstream calls onSubscribe at which
     * case the mainThread Disposable will be immediately disposed.
     */
    @Override
    public final void dispose() {
        if (DisposableHelper.dispose(s)) {
            resources.dispose();
        }
    }

    /**
     * Returns true if this ResourceObserver has been disposed/cancelled.
     * @return true if this ResourceObserver has been disposed/cancelled
     */
    @Override
    public final boolean isDisposed() {
        return DisposableHelper.isDisposed(s.get());
    }
}
