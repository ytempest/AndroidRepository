/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.greenrobot.eventbus;

import android.os.Looper;
import android.support.annotation.NonNull;

import org.greenrobot.eventbus.android.AndroidLogger;
import org.greenrobot.eventbus.meta.SubscriberInfoIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates EventBus instances with custom parameters and also allows to install a custom default EventBus instance.
 * Create a new builder using {@link EventBus#builder()}.
 */
public class EventBusBuilder {
    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    boolean logSubscriberExceptions = true;
    boolean logNoSubscriberMessages = true;
    boolean sendSubscriberExceptionEvent = true;
    boolean sendNoSubscriberEvent = true;
    boolean throwSubscriberException;
    boolean eventInheritance = true;
    boolean ignoreGeneratedIndex;
    boolean strictMethodVerification;
    ExecutorService executorService = DEFAULT_EXECUTOR_SERVICE;
    List<Class<?>> skipMethodVerificationForClasses;
    /**
     * 这是一个索引类集合，可以存储多个索引类，默认为null，需要在外部进行设置才能使用
     */
    List<SubscriberInfoIndex> subscriberInfoIndexes;
    Logger logger;
    MainThreadSupport mainThreadSupport;

    EventBusBuilder() {
    }

    /**
     * Default: true
     */
    public EventBusBuilder logSubscriberExceptions(boolean logSubscriberExceptions) {
        this.logSubscriberExceptions = logSubscriberExceptions;
        return this;
    }

    /**
     * Default: true
     */
    public EventBusBuilder logNoSubscriberMessages(boolean logNoSubscriberMessages) {
        this.logNoSubscriberMessages = logNoSubscriberMessages;
        return this;
    }

    /**
     * Default: true
     */
    public EventBusBuilder sendSubscriberExceptionEvent(boolean sendSubscriberExceptionEvent) {
        this.sendSubscriberExceptionEvent = sendSubscriberExceptionEvent;
        return this;
    }

    /**
     * Default: true
     */
    public EventBusBuilder sendNoSubscriberEvent(boolean sendNoSubscriberEvent) {
        this.sendNoSubscriberEvent = sendNoSubscriberEvent;
        return this;
    }

    /**
     * Fails if an subscriber throws an exception (default: false).
     * <p/>
     * Tip: Use this with BuildConfig.DEBUG to let the app crash in DEBUG mode (only). This way, you won't miss
     * exceptions during development.
     */
    public EventBusBuilder throwSubscriberException(boolean throwSubscriberException) {
        this.throwSubscriberException = throwSubscriberException;
        return this;
    }

    /**
     * By default, EventBus considers the event class hierarchy (subscribers to super classes will be notified).
     * Switching this feature off will improve posting of events. For simple event classes extending Object directly,
     * we measured a speed up of 20% for event posting. For more complex event hierarchies, the speed up should be
     * >20%.
     * <p/>
     * However, keep in mind that event posting usually consumes just a small proportion of CPU time inside an app,
     * unless it is posting at high rates, e.g. hundreds/thousands of events per second.
     */
    public EventBusBuilder eventInheritance(boolean eventInheritance) {
        this.eventInheritance = eventInheritance;
        return this;
    }


    /**
     * Provide a custom thread pool to EventBus used for async and background event delivery. This is an advanced
     * setting to that can break things: ensure the given ExecutorService won't get stuck to avoid undefined behavior.
     */
    public EventBusBuilder executorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * Method name verification is done for methods starting with onEvent to avoid typos; using this method you can
     * exclude subscriber classes from this check. Also disables checks for method modifiers (public, not static nor
     * abstract).
     */
    public EventBusBuilder skipMethodVerificationFor(Class<?> clazz) {
        if (skipMethodVerificationForClasses == null) {
            skipMethodVerificationForClasses = new ArrayList<>();
        }
        skipMethodVerificationForClasses.add(clazz);
        return this;
    }

    /**
     * Forces the use of reflection even if there's a generated index (default: false).
     * 如果设置为true表示忽略使用索引类
     */
    public EventBusBuilder ignoreGeneratedIndex(boolean ignoreGeneratedIndex) {
        this.ignoreGeneratedIndex = ignoreGeneratedIndex;
        return this;
    }

    /**
     * Enables strict method verification (default: false).
     */
    public EventBusBuilder strictMethodVerification(boolean strictMethodVerification) {
        this.strictMethodVerification = strictMethodVerification;
        return this;
    }

    /**
     * 添加索引对象到索引类集合中，这个索引类是在编译的时候生成的
     */
    public EventBusBuilder addIndex(SubscriberInfoIndex index) {
        if (subscriberInfoIndexes == null) {
            subscriberInfoIndexes = new ArrayList<>();
        }
        // 添加到索引类集合
        subscriberInfoIndexes.add(index);
        return this;
    }

    /**
     * Set a specific log handler for all EventBus logging.
     * <p/>
     * By default all logging is via {@link android.util.Log} but if you want to use EventBus
     * outside the Android environment then you will need to provide another log target.
     */
    public EventBusBuilder logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    Logger getLogger() {
        if (logger != null) {
            return logger;
        } else {
            // also check main looper to see if we have "good" Android classes (not Stubs etc.)
            return AndroidLogger.isAndroidLogAvailable() && getAndroidMainLooperOrNull() != null
                    ? new AndroidLogger("EventBus") :
                    new Logger.SystemOutLogger();
        }
    }


    /**
     * 获取Android的主线程支持类，根据是否可以获取到主线程的Looper对象进行判断，如果可以获取到，那
     * 返回一个 MainThreadSupport.AndroidHandlerMainThreadSupport对象，否则返回一个null
     */
    MainThreadSupport getMainThreadSupport() {
        if (mainThreadSupport != null) {
            return mainThreadSupport;

        } else if (AndroidLogger.isAndroidLogAvailable()) {
            // 获取Android主线程的Looper对象，如果获取不到则返回null
            Object looperOrNull = getAndroidMainLooperOrNull();
            return looperOrNull == null ? null :
                    new MainThreadSupport.AndroidHandlerMainThreadSupport((Looper) looperOrNull);
        } else {
            return null;
        }
    }

    /**
     * 获取Android的主线程的Looper对象，如果获取不到则返回null
     */
    Object getAndroidMainLooperOrNull() {
        try {
            return Looper.getMainLooper();
        } catch (RuntimeException e) {
            // Not really a functional Android (e.g. "Stub!" maven dependencies)
            return null;
        }
    }

    /**
     * Installs the default EventBus returned by {@link EventBus#getDefault()} using this builders' values. Must be
     * done only once before the first usage of the default EventBus.
     *
     * @throws EventBusException if there's already a default EventBus instance in place
     */
    public EventBus installDefaultEventBus() {
        synchronized (EventBus.class) {
            if (EventBus.defaultInstance != null) {
                throw new EventBusException("Default instance already exists." +
                        " It may be only set once before it's used the first time to ensure consistent behavior.");
            }
            EventBus.defaultInstance = build();
            return EventBus.defaultInstance;
        }
    }

    /**
     * Builds an EventBus based on the current configuration.
     */
    public EventBus build() {
        return new EventBus(this);
    }

}
