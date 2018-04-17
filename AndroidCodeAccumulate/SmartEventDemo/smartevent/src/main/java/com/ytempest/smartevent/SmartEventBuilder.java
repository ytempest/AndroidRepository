package com.ytempest.smartevent;


import com.ytempest.smartevent.meta.SubscriberInfoIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ytempest
 *         Description：
 */
public class SmartEventBuilder {

    /**
     * 默认线程池，线程池的配置跟系统 CachedThreadPool一样
     */
    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = new ThreadPoolExecutor(
            0, Integer.MAX_VALUE, 60L,
            TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                // 线程的id
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "SmartEventThreadPool#" + mCount.getAndIncrement());
                }
            });

    /**
     * 索引类列表
     */
    List<SubscriberInfoIndex> mSubscriberInfoIndexes = null;
    /**
     * 是否忽略索引
     */
    boolean isIgnoreGeneratedIndex = false;

    ExecutorService mExecutorService = DEFAULT_EXECUTOR_SERVICE;
    boolean isEventInheritance = true;


    public SmartEventBuilder setExecutorService(ExecutorService executorService) {
        this.mExecutorService = executorService;
        return this;
    }

    public SmartEventBuilder setEventInheritance(boolean eventInheritance) {
        this.isEventInheritance = eventInheritance;
        return this;
    }

    public SmartEventBuilder addSubscriberInfoIndex(SubscriberInfoIndex subscriberInfoIndex) {
        if (mSubscriberInfoIndexes == null) {
            mSubscriberInfoIndexes = new ArrayList<>();
        }
        mSubscriberInfoIndexes.add(subscriberInfoIndex);
        return this;
    }

    public SmartEventBuilder setIgnoreGeneratedIndex(boolean ignoreGeneratedIndex) {
        isIgnoreGeneratedIndex = ignoreGeneratedIndex;
        return this;
    }

    public SmartEvent build() {
        return new SmartEvent(this);
    }

    /**
     * 安装默认的 SmartEventBuilder，除了设置的一些配置，其他配置使用默认的
     */
    public SmartEvent installDefaultSmartEvent() {
        synchronized (SmartEvent.class) {
            if (SmartEvent.mDefaultInstance != null) {
                throw new SmartEventException("SmartEvent default instance already exists. "
                        + "It may be only set once before it's used the first time to ensure consistent behavior.");
            }
            SmartEvent.mDefaultInstance = build();

            return SmartEvent.mDefaultInstance;
        }
    }
}
