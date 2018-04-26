package com.ytempest.rxjavaanalysis.rxjava.scheduler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @author ytempest
 *         Description：
 */
public class Schedulers {

    private static final String IO_THREAD_POOL_NAME = "RxJavaIoThread";

    public static Scheduler io() {
        return new IoThreadScheduler();
    }

    public static Scheduler mainThread() {
        return new MainThreadScheduler();
    }

    private static class IoThreadScheduler extends Scheduler {

        private final ScheduledExecutorService mExecutorService;

        IoThreadScheduler() {
            mExecutorService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, IO_THREAD_POOL_NAME);
                }
            });
        }

        @Override
        public void schedule(Runnable run) {
            mExecutorService.submit(run);
        }
    }


    private static class MainThreadScheduler extends Scheduler {

        private Handler handler;

        MainThreadScheduler() {
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void schedule(Runnable run) {
            Message message = Message.obtain(handler, run);
            handler.sendMessage(message);
        }
    }
}

