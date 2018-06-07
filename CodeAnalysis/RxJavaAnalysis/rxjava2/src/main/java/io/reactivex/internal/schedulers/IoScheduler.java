/**
 * Copyright (c) 2016-present, RxJava Contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.reactivex.internal.schedulers;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.*;
import io.reactivex.internal.disposables.EmptyDisposable;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Scheduler that creates and caches a set of thread pools and reuses them if possible.
 */
public final class IoScheduler extends Scheduler {
    // IoScheduler 默认的线程池名字
    private static final String WORKER_THREAD_NAME_PREFIX = "RxCachedThreadScheduler";
    // IoScheduler 默认的线程工厂
    static final RxThreadFactory WORKER_THREAD_FACTORY;

    private static final String EVICTOR_THREAD_NAME_PREFIX = "RxCachedWorkerPoolEvictor";
    static final RxThreadFactory EVICTOR_THREAD_FACTORY;

    // 线程存活时间
    private static final long KEEP_ALIVE_TIME = 60;
    // 线程存活时间单位
    private static final TimeUnit KEEP_ALIVE_UNIT = TimeUnit.SECONDS;

    static final ThreadWorker SHUTDOWN_THREAD_WORKER;
    final ThreadFactory threadFactory;
    // 为 NONE对象（CachedWorkerPool对象）创建一个原子性引用
    final AtomicReference<CachedWorkerPool> pool;

    /**
     * The name of the system property for setting the thread priority for this Scheduler.
     */
    private static final String KEY_IO_PRIORITY = "rx2.io-priority";

    static final CachedWorkerPool NONE;


    //  <*****************    在静态代码块初始化一些资源     *****************>
    static {

        SHUTDOWN_THREAD_WORKER = new ThreadWorker(new RxThreadFactory("RxCachedThreadSchedulerShutdown"));
        SHUTDOWN_THREAD_WORKER.dispose();

        int priority = Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY,
                Integer.getInteger(KEY_IO_PRIORITY, Thread.NORM_PRIORITY)));

        // 创建 IoScheduler 默认的线程工厂
        WORKER_THREAD_FACTORY = new RxThreadFactory(WORKER_THREAD_NAME_PREFIX, priority);

        EVICTOR_THREAD_FACTORY = new RxThreadFactory(EVICTOR_THREAD_NAME_PREFIX, priority);

        // 创建 CacheWorkerPoll，用于管理 ThreadWorker
        NONE = new CachedWorkerPool(0, null, WORKER_THREAD_FACTORY);
        NONE.shutdown();
    }

    /**
     * Description：这个应该是 ThreadWorker的一个池，用于缓存ThreadWorker；为什么要缓存 ThreadWorker，
     * 因为每一个 ThreadWorker都会创建线程池，这就意味着创建一个 ThreadWorker开销很大；由源码可知道，
     * 每切换到这个 IoScheduler就会构建一个 EventLoopWorker，在这个构造方法中会获取一个 ThreadWorker，
     * 这个ThreadWorker会处理线程调度的任务；所以不能每构建一个 EventLoopWorker就创建一个ThreadWorker，
     * 最好就从缓存中获取
     */
    static final class CachedWorkerPool implements Runnable {
        private final long keepAliveTime;
        // 这是一个线程安全的 ThreadWorker缓存，之所以要缓存，可能是因为创建 ThreadWorker的开销较大
        private final ConcurrentLinkedQueue<ThreadWorker> expiringWorkerQueue;
        final CompositeDisposable allWorkers;

        private final ScheduledExecutorService evictorService;
        private final Future<?> evictorTask;
        // 这个线程工厂是 IoScheduler中的线程工厂，是从那边传过来的
        private final ThreadFactory threadFactory;

        CachedWorkerPool(long keepAliveTime, TimeUnit unit, ThreadFactory threadFactory) {
            this.keepAliveTime = unit != null ? unit.toNanos(keepAliveTime) : 0L;
            this.expiringWorkerQueue = new ConcurrentLinkedQueue<ThreadWorker>();
            this.allWorkers = new CompositeDisposable();
            this.threadFactory = threadFactory;

            ScheduledExecutorService evictor = null;
            Future<?> task = null;
            if (unit != null) {
                evictor = Executors.newScheduledThreadPool(1, EVICTOR_THREAD_FACTORY);
                task = evictor.scheduleWithFixedDelay(this, this.keepAliveTime, this.keepAliveTime, TimeUnit.NANOSECONDS);
            }
            evictorService = evictor;
            evictorTask = task;
        }

        @Override
        public void run() {
            evictExpiredWorkers();
        }

        /**
         * 从 expiringWorkerQueue缓存中获取 ThreadWorker，如果没有就创建一个
         */
        ThreadWorker get() {
            if (allWorkers.isDisposed()) {
                return SHUTDOWN_THREAD_WORKER;
            }
            // 如果有缓存，就从缓存中获取一个 ThreadWorker
            while (!expiringWorkerQueue.isEmpty()) {
                ThreadWorker threadWorker = expiringWorkerQueue.poll();
                if (threadWorker != null) {
                    return threadWorker;
                }
            }

            // 没有缓存，所以创建一个新的，然后返回
            ThreadWorker w = new ThreadWorker(threadFactory);
            allWorkers.add(w);
            return w;
        }

        void release(ThreadWorker threadWorker) {
            // Refresh expire time before putting worker back in pool
            threadWorker.setExpirationTime(now() + keepAliveTime);

            expiringWorkerQueue.offer(threadWorker);
        }

        void evictExpiredWorkers() {
            if (!expiringWorkerQueue.isEmpty()) {
                long currentTimestamp = now();

                for (ThreadWorker threadWorker : expiringWorkerQueue) {
                    if (threadWorker.getExpirationTime() <= currentTimestamp) {
                        if (expiringWorkerQueue.remove(threadWorker)) {
                            allWorkers.remove(threadWorker);
                        }
                    } else {
                        // Queue is ordered with the worker that will expire first in the beginning, so when we
                        // find a non-expired worker we can stop evicting.
                        break;
                    }
                }
            }
        }

        long now() {
            return System.nanoTime();
        }

        void shutdown() {
            allWorkers.dispose();
            if (evictorTask != null) {
                evictorTask.cancel(true);
            }
            if (evictorService != null) {
                evictorService.shutdownNow();
            }
        }
    }

    /**
     * 如果没有指定 IoScheduler的线程工厂，那么就使用默认的 WORKER_THREAD_FACTORY线程工厂
     */
    public IoScheduler() {
        // WORKER_THREAD_FACTORY 会在本类中的静态代码块中被初始化
        this(WORKER_THREAD_FACTORY);
    }

    /**
     * 使用指定的线程工厂去创建 IoScheduler
     *
     * @param threadFactory IoScheduler的线程工厂
     */
    public IoScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        // 为 NONE对象（CachedWorkerPool对象）创建一个原子性引用，保证
        // 这个NONE对象的读和写都是原子性的，可以通过 pool.get()方法获取这个 NONE对象
        this.pool = new AtomicReference<CachedWorkerPool>(NONE);
        start();
    }

    @Override
    public void start() {
        CachedWorkerPool update = new CachedWorkerPool(KEEP_ALIVE_TIME, KEEP_ALIVE_UNIT, threadFactory);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }

    @Override
    public void shutdown() {
        for (; ; ) {
            CachedWorkerPool curr = pool.get();
            if (curr == NONE) {
                return;
            }
            if (pool.compareAndSet(curr, NONE)) {
                curr.shutdown();
                return;
            }
        }
    }

    /**
     * 这个方法会由 Scheduler进行调用以获取相应的 Worker
     */
    @NonNull
    @Override
    public Worker createWorker() {
        // pool.get()会返回一个 CachedWorkerPool对象，然后用这个对象实例化 EventLoopWorker
        return new EventLoopWorker(pool.get());
    }

    public int size() {
        return pool.get().allWorkers.size();
    }

    /**
     * Description：这是一个事件循环的工作类，主要作用获取 ThreadWorker（从 CacheWorkerPool缓存中获取
     * 或者 new一个）将 Runnable执行单元分发给 ThreadWorker进行执行
     */
    static final class EventLoopWorker extends Scheduler.Worker {
        private final CompositeDisposable tasks;
        // 在CacheWorkerPool引用，用于从其缓存中获取 TreadWorker
        private final CachedWorkerPool pool;

        // 这个对象是在 CachedWorkerPool缓存中获取的，如果没有缓存就new
        // 这 EventLoopWorker构造的时候就会实例化这个 threadWorker
        private final ThreadWorker threadWorker;

        final AtomicBoolean once = new AtomicBoolean();

        EventLoopWorker(CachedWorkerPool pool) {
            this.pool = pool;
            this.tasks = new CompositeDisposable();
            // 获取 ThreadWorker
            this.threadWorker = pool.get();
        }

        @Override
        public void dispose() {
            if (once.compareAndSet(false, true)) {
                tasks.dispose();

                // releasing the pool should be the last action
                pool.release(threadWorker);
            }
        }

        @Override
        public boolean isDisposed() {
            return once.get();
        }


        /**
         * 在这个方法中对任务进行分发，将任务分发给ThreadWorker进行处理，本身不参与任务的执行
         *
         * @param action 执行单元，封装了被观察者对象要在线程中执行的逻辑
         */
        @NonNull
        @Override
        public Disposable schedule(@NonNull Runnable action, long delayTime, @NonNull TimeUnit unit) {
            if (tasks.isDisposed()) {
                // don't schedule, we are unsubscribed
                return EmptyDisposable.INSTANCE;
            }


            // ThreadWorker 继承于 NewThreadWorker，所以会调用 NewThreadWorker中的 scheduleActual方法
            return threadWorker.scheduleActual(action, delayTime, unit, tasks);
        }
    }


    /**
     * Description：这个类会使用 threadFactory创建一个线程池（这个线程池只有一个Thread），然后
     * 执行 Runnable执行单元
     */
    static final class ThreadWorker extends NewThreadWorker {
        private long expirationTime;

        ThreadWorker(ThreadFactory threadFactory) {
            super(threadFactory);
            this.expirationTime = 0L;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public void setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
        }
    }
}
