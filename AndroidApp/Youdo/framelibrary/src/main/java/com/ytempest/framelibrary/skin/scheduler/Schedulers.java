package com.ytempest.framelibrary.skin.scheduler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author ytempest
 *         Description：用于创建不同类型的线程，使用的是静态内部类的单例模式
 */
public class Schedulers {


    private Schedulers() {
    }

    public static Scheduler asyncThread() {
        return new AsyncThreadScheduler();
    }

    public static Scheduler mainThread() {
        return new MainThreadScheduler();
    }

    private static class AsyncThreadScheduler implements Scheduler {
        private final ExecutorService mExecutorService;

        AsyncThreadScheduler() {
            mExecutorService = new ThreadPoolExecutor(0, 10,
                    30, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread result = new Thread(r, "SkinDispatcher");
                    result.setDaemon(false);
                    return result;
                }
            });
        }

        @Override
        public void schedule(Runnable task) {
            mExecutorService.execute(task);
        }

    }


    private static class MainThreadScheduler implements Scheduler {
        private final Handler mHandler;

        MainThreadScheduler() {
            mHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void schedule(Runnable task) {
            Message message = Message.obtain(mHandler, task);
            mHandler.sendMessage(message);
        }
    }
}
