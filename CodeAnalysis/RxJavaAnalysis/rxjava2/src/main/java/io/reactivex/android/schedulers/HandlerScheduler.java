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
import android.os.Message;

import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.plugins.RxJavaPlugins;

import java.util.concurrent.TimeUnit;

/**
 * Description：这个实现了 Scheduler的 Android的主线程
 */
final class HandlerScheduler extends Scheduler {
    // 主线程的 Handler
    private final Handler handler;

    HandlerScheduler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        if (run == null) throw new NullPointerException("run == null");
        if (unit == null) throw new NullPointerException("unit == null");

        run = RxJavaPlugins.onSchedule(run);
        ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);
        handler.postDelayed(scheduled, unit.toMillis(delay));
        return scheduled;
    }

    @Override
    public Worker createWorker() {
        return new HandlerWorker(handler);
    }


    /**
     * Description：
     */
    private static final class HandlerWorker extends Worker {
        private final Handler handler;

        private volatile boolean disposed;

        HandlerWorker(Handler handler) {
            this.handler = handler;
        }


        /**
         * 在这个方法中会执行线程调度的执行单元
         *
         * @param run   要进行线程调度的执行单元
         * @param delay 延迟执行的时间
         * @param unit  延迟执行的时间单位
         */
        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (run == null) throw new NullPointerException("run == null");
            if (unit == null) throw new NullPointerException("unit == null");

            if (disposed) {
                return Disposables.disposed();
            }

            // 交给 RxJavaPlugins判断用户是否要拦截这个 Runnable执行单元
            run = RxJavaPlugins.onSchedule(run);

            // handler：这个主线程的 handler
            // run：这是要要调度到指定线程后的执行单元，是一个 runnable对象
            // 使用委派模式去包裹这个 run，这样就可以监听 run对象 run()方法的的执行情况
            ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);

            Message message = Message.obtain(handler, scheduled);
            message.obj = this; // Used as token for batch disposal of this worker's runnables.

            // 发送一个带有Runnable对象的消息给主线程中，当主线程收到消息后就会执行这个Runnable
            handler.sendMessageDelayed(message, unit.toMillis(delay));

            // Re-check disposed state for removing in case we were racing a call to dispose().
            if (disposed) {
                handler.removeCallbacks(scheduled);
                return Disposables.disposed();
            }

            return scheduled;
        }

        @Override
        public void dispose() {
            disposed = true;
            handler.removeCallbacksAndMessages(this /* token */);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }

    /**
     * Description：使用了一种委派模式去封装 Runnable对象，以监听其 run()方法的执行情况，如果产生异常
     * 就处理异常
     */
    private static final class ScheduledRunnable implements Runnable, Disposable {
        private final Handler handler;
        private final Runnable delegate;

        private volatile boolean disposed;

        ScheduledRunnable(Handler handler, Runnable delegate) {
            this.handler = handler;
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                delegate.run();
            } catch (Throwable t) {
                RxJavaPlugins.onError(t);
            }
        }

        @Override
        public void dispose() {
            disposed = true;
            handler.removeCallbacks(this);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
