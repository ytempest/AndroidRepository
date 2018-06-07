/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.android.schedulers;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;

/**
 * Android-specific Schedulers.
 */
public final class AndroidSchedulers {

    /**
     * Description：使用静态内部类的形式实现懒加载，在使用到 DEFAULT属性的时候才加载这个类
     */
    private static final class MainHolder {
        static final Scheduler DEFAULT = new HandlerScheduler(new Handler(Looper.getMainLooper()));
    }

    /**
     * 这个是RxJava默认使用的的 Android的主线程，使用了RxAndroidPlugins.initMainThreadScheduler()方法
     * 判断是否用户要拦截这个 mainThread的初始化过程
     */
    private static final Scheduler MAIN_THREAD = RxAndroidPlugins.initMainThreadScheduler(
            new Callable<Scheduler>() {
                @Override
                public Scheduler call() throws Exception {
                    return MainHolder.DEFAULT;
                }
            });

    /**
     * A {@link Scheduler} which executes actions on the Android mainThread thread.
     * 先将初始化好的 Android主线程的 Scheduler交个RxAndroidPlugins判断用户是否要拦截这个 Scheduler
     */
    public static Scheduler mainThread() {
        return RxAndroidPlugins.onMainThreadScheduler(MAIN_THREAD);
    }

    /**
     * A {@link Scheduler} which executes actions on {@code looper}.
     */
    public static Scheduler from(Looper looper) {
        if (looper == null) throw new NullPointerException("looper == null");
        return new HandlerScheduler(new Handler(looper));
    }

    private AndroidSchedulers() {
        throw new AssertionError("No instances.");
    }
}
