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
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.BasicFuseableObserver;


/**
 * map 转换器采用的是一种代理，通过代理执行onSubscribe、onNext、onError、onComplete方法的observer
 * 对象，拦截到原来的 observer对象中之后，先对数据进行自己的处理，然后再交给原来的观察者对象处理
 * <p>
 * 使用 map 转换器就会创建一个ObservableMap对象；这个类封装了上一个Observable对象，同时保存了对一
 * 个Observable对象的事件进行转换的map转换函数；等到创建 MapObserver 对象的时候会把保存的转换函数
 * 传递给 MapObserver 对象
 * <p>
 * 这个对象通过两样东西联系上一个Observable对象以及下一个被观察者对象
 * 1、通过持有上一个 Observable 对象的引用
 * 2、通过在 subscribeActual 方法中创建一个 MapObserver 对象，让这个 MapObserver 对象持有
 * 下一个被观察者对象的引用
 *
 * @param <T> 上一个Observable对象的事件
 * @param <U> 通过map转换器要转换成的目的事件类型
 */
public final class ObservableMap<T, U> extends AbstractObservableWithUpstream<T, U> {
    /**
     * 实现转换的一个接口对象
     */
    final Function<? super T, ? extends U> function;

    /**
     * @param source   被观察者对象
     * @param function 实现转换的一个接口对象
     */
    public ObservableMap(ObservableSource<T> source, Function<? super T, ? extends U> function) {
        super(source);
        this.function = function;
    }

    /**
     * 通过这个方法创建一个代理对象MapObserver代理原来的观察者，然后让被观察者连接这一个代理
     * 对象，如果不使用subscribe连接这一个代理对象，那么被观察者就无法找到观察者对象
     *
     * @param t 下一个观察者对象
     */
    @Override
    public void subscribeActual(Observer<? super U> t) {
        Log.e("TAG", "ObservableMap: ------> "+Thread.currentThread());
        // 这里会调用 ObservableJust的subscribe方法
        source.subscribe(new MapObserver<T, U>(t, function));
    }

    /**
     * Description：使用map转换器之后，会使用这个类来封装下一个观察者对象以及 事件转换函数；
     * 然后通过一种代理的方式，去拦截下一个观察者对象的 onSubscribe、onNext、onError、onComplete
     * 方法，经过自己的一些处理逻辑之后再让下一个观察者对象处理这些方法
     * <p>
     * 而实现map装换器的原理在于：拦截下一个观察者对象的 onNext方法，通过事件转换函数把上一个被
     * 观察者对象的事件转换成目的事件，然后再把这个目的事件作为参数传递到下一个观察者的 onNext
     * 方法中
     */
    static final class MapObserver<T, U> extends BasicFuseableObserver<T, U> {
        final Function<? super T, ? extends U> mapper;

        /**
         * @param actual 下一个观察者对象
         * @param mapper 转换函数
         */
        MapObserver(Observer<? super U> actual, Function<? super T, ? extends U> mapper) {
            super(actual);
            this.mapper = mapper;
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }

            if (sourceMode != NONE) {
                actual.onNext(null);
                return;
            }

            U v;

            try {
                v = ObjectHelper.requireNonNull(mapper.apply(t), "The mapper function returned a null value.");
            } catch (Throwable ex) {
                fail(ex);
                return;
            }
            actual.onNext(v);
        }

        @Override
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Nullable
        @Override
        public U poll() throws Exception {
            T t = qs.poll();
            return t != null ? ObjectHelper.<U>requireNonNull(mapper.apply(t), "The mapper function returned a null value.") : null;
        }
    }
}
