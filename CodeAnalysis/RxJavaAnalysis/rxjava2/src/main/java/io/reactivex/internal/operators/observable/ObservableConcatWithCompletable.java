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

package io.reactivex.internal.operators.observable;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;

/**
 * Subscribe to a mainThread ObservableSource first, then when it completes normally, subscribe to a Single,
 * signal its success value followed by a completion or signal its error as is.
 * @param <T> the element type of the mainThread source and output type
 * @since 2.1.10 - experimental
 */
public final class ObservableConcatWithCompletable<T> extends AbstractObservableWithUpstream<T, T> {

    final CompletableSource other;

    public ObservableConcatWithCompletable(Observable<T> source, CompletableSource other) {
        super(source);
        this.other = other;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new ConcatWithObserver<T>(observer, other));
    }

    static final class ConcatWithObserver<T>
    extends AtomicReference<Disposable>
    implements Observer<T>, CompletableObserver, Disposable {

        private static final long serialVersionUID = -1953724749712440952L;

        final Observer<? super T> actual;

        CompletableSource other;

        boolean inCompletable;

        ConcatWithObserver(Observer<? super T> actual, CompletableSource other) {
            this.actual = actual;
            this.other = other;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.setOnce(this, d) && !inCompletable) {
                actual.onSubscribe(this);
            }
        }

        @Override
        public void onNext(T t) {
            actual.onNext(t);
        }

        @Override
        public void onError(Throwable e) {
            actual.onError(e);
        }

        @Override
        public void onComplete() {
            if (inCompletable) {
                actual.onComplete();
            } else {
                inCompletable = true;
                DisposableHelper.replace(this, null);
                CompletableSource cs = other;
                other = null;
                cs.subscribe(this);
            }
        }

        @Override
        public void dispose() {
            DisposableHelper.dispose(this);
        }

        @Override
        public boolean isDisposed() {
            return DisposableHelper.isDisposed(get());
        }
    }
}
