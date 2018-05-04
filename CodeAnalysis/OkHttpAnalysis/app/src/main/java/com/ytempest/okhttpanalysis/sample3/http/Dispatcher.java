package com.ytempest.okhttpanalysis.sample3.http;

import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ytempest
 *         Description：处理网络请求的线程池
 */
public class Dispatcher {

    private ThreadPoolExecutor executorService;

    public Dispatcher() {
    }

    public Dispatcher(ThreadPoolExecutor executorService) {
        this.executorService = executorService;
    }

    private synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread result = new Thread(r, "IOkHttpThread");
                    result.setDaemon(false);
                    return result;
                }
            });
        }
        return executorService;
    }

    synchronized void enqueue(RealCall.AsyncCall asyncCall) {
        executorService().execute(asyncCall);
    }
}
