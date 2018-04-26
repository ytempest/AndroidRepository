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

import android.util.Log;

import io.reactivex.*;
import io.reactivex.internal.fuseable.ScalarCallable;
import io.reactivex.internal.operators.observable.ObservableScalarXMap.ScalarDisposable;

/**
 * 使用just方法构建被被观察者时，会使用这个代理类代理被观察者
 *
 * @param <T> 被观察者的事件类型
 */
public final class ObservableJust<T> extends Observable<T> implements ScalarCallable<T> {

    /**
     * 这个值就是要构建被观察者的事件
     */
    private final T value;

    public ObservableJust(final T value) {
        this.value = value;
    }


    /**
     * 当被观察者发生变化的时候会调用这个方法去通知观察者
     *
     * @param s 下一个观察者对象
     */
    @Override
    protected void subscribeActual(Observer<? super T> s) {
        // 构建通知开关
        ScalarDisposable<T> sd = new ScalarDisposable<T>(s, value);
        // 调用观察者对象的方法，这个方法会在事件发生改变之前调用
        // 可以通过这个方法对事件进行取消等操作
        s.onSubscribe(sd);
        // 发送事件，调用这个方法后会执行观察者的 onNext 方法
        sd.run();
    }

    @Override
    public T call() {
        return value;
    }
}
