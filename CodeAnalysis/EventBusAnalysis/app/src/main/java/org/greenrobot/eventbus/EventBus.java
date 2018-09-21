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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;


public class EventBus {

    /**
     * Log tag, apps may override it.
     */
    public static String TAG = "EventBus";

    static volatile EventBus defaultInstance;

    private static final EventBusBuilder DEFAULT_BUILDER = new EventBusBuilder();
    /**
     * 这个用来存储每一事件的所有父类和父类实现的所有接口（作缓存用）
     * key：事件，即post方法传入的参数的Class
     * value：事件的所有父类和接口（列表第一个值是key自身）
     */
    private static final Map<Class<?>, List<Class<?>>> eventTypesCache = new HashMap<>();
    /**
     * 存储了每一个订阅对象的所有方法信息
     * key：订阅对象（如MainActivity）
     * value：订阅对象中所有标记了Subscribe注解的方法的参数列表，即订阅的事件类型
     */
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    /**
     * 存储每一个事件类型所对应的所有方法的Map
     * key：事件类型对象（即：标记了Subscribe注解方法的参数）
     * value：事件类型对象对应的所有订阅方法（即参数都是事件类型（如String，Message）的方法）
     */
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;
    /**
     * 存储了每一种事件类型对应的事件对象
     * key：事件类型
     * value：事件对象本身
     */
    private final Map<Class<?>, Object> stickyEvents;

    private final ThreadLocal<PostingThreadState> currentPostingThreadState = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

    // @Nullable
    private final MainThreadSupport mainThreadSupport;
    // @Nullable
    private final Poster mainThreadPoster;
    private final BackgroundPoster backgroundPoster;
    private final AsyncPoster asyncPoster;
    private final SubscriberMethodFinder subscriberMethodFinder;
    private final ExecutorService executorService;

    private final boolean throwSubscriberException;
    private final boolean logSubscriberExceptions;
    private final boolean logNoSubscriberMessages;
    private final boolean sendSubscriberExceptionEvent;
    private final boolean sendNoSubscriberEvent;
    /**
     * 表示事件的所有的父类以及接口的订阅方法是否可以接收该事件
     */
    private final boolean eventInheritance;

    private final int indexCount;
    private final Logger logger;

    /**
     * Convenience singleton for apps using a process-wide EventBus instance.
     */
    public static EventBus getDefault() {
        EventBus instance = defaultInstance;
        if (instance == null) {
            synchronized (EventBus.class) {
                instance = EventBus.defaultInstance;
                if (instance == null) {
                    instance = EventBus.defaultInstance = new EventBus();
                }
            }
        }
        return instance;
    }

    public static EventBusBuilder builder() {
        return new EventBusBuilder();
    }

    /**
     * For unit test primarily.
     */
    public static void clearCaches() {
        SubscriberMethodFinder.clearCaches();
        eventTypesCache.clear();
    }

    /**
     * Creates a new EventBus instance; each instance is a separate scope in which events are delivered. To use a
     * central bus, consider {@link #getDefault()}.
     */
    public EventBus() {
        this(DEFAULT_BUILDER);
    }

    EventBus(EventBusBuilder builder) {
        logger = builder.getLogger();
        subscriptionsByEventType = new HashMap<>();
        typesBySubscriber = new HashMap<>();
        stickyEvents = new ConcurrentHashMap<>();
        // 通过EventBusBuilder的方法获取主线程的支持类
        mainThreadSupport = builder.getMainThreadSupport();
        // 通过主线程的支持类获取主线程的Handler
        mainThreadPoster = mainThreadSupport != null ? mainThreadSupport.createPoster(this) : null;
        backgroundPoster = new BackgroundPoster(this);
        asyncPoster = new AsyncPoster(this);
        indexCount = builder.subscriberInfoIndexes != null ? builder.subscriberInfoIndexes.size() : 0;
        subscriberMethodFinder = new SubscriberMethodFinder(builder.subscriberInfoIndexes,
                builder.strictMethodVerification, builder.ignoreGeneratedIndex);
        logSubscriberExceptions = builder.logSubscriberExceptions;
        logNoSubscriberMessages = builder.logNoSubscriberMessages;
        sendSubscriberExceptionEvent = builder.sendSubscriberExceptionEvent;
        sendNoSubscriberEvent = builder.sendNoSubscriberEvent;
        throwSubscriberException = builder.throwSubscriberException;
        eventInheritance = builder.eventInheritance;
        executorService = builder.executorService;
    }

    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
     * are no longer interested in receiving events.
     * <p/>
     * Subscribers have event handling methods that must be annotated by {@link Subscribe}.
     * The {@link Subscribe} annotation also allows configuration like {@link
     * ThreadMode} and priority.
     */
    public void register(Object subscriber) {
        Class<?> subscriberClass = subscriber.getClass();
        // 1、获取订阅对象的所有目标方法（标志了Subscribe注解的方法）
        List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
        synchronized (this) {
            // 2、遍历所有目标方法，然后把其添加到subscriptionsByEventType和 typesBySubscriber集合中
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    /**
     * 1、根据订阅方法所属的订阅对象，将其存储到 typesBySubscriber 中
     * 2、根据订阅方法的参数类型将每一个订阅方法分类存储到 subscriptionsByEventType中
     * 3、这个方法必须在 synchronized 中的代码块中执行，否则可能会导致线程不同步
     *
     * @param subscriber       订阅对象
     * @param subscriberMethod 订阅对象中标记了 Subscribe注解的一个方法
     */
    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        // 1、获取订阅方法的参数类型
        Class<?> eventType = subscriberMethod.eventType;
        // 2、封装订阅方法，一个Subscription对象对应一个订阅方法
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        // 3、从subscriptionsByEventType集合中获取事件类型对应的所有订阅方法
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions == null) {
            // 如果为空，则表明是第一次添加
            subscriptions = new CopyOnWriteArrayList<>();
            subscriptionsByEventType.put(eventType, subscriptions);
        } else {
            // 如果不为空，则说明已经添加过了，抛异常
            // 也就是说：如果一个对象注册了两次，那么就会抛异常
            if (subscriptions.contains(newSubscription)) {
                throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event "
                        + eventType);
            }
        }

        // 4、遍历订阅方法的事件类型的所有的订阅方法，根据优先级将订阅方法添加到
        // subscriptionsByEventType列表中合适的位置
        int size = subscriptions.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
                subscriptions.add(i, newSubscription);
                break;
            }
        }

        // 5、将订阅方法按其所属的对象存储到typesBySubscriber集合中
        List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
        if (subscribedEvents == null) {
            subscribedEvents = new ArrayList<>();
            typesBySubscriber.put(subscriber, subscribedEvents);
        }
        subscribedEvents.add(eventType);

        // 6、如果这个方法订阅的是一个粘性事件，那么在register的时候就处理该事件
        if (subscriberMethod.sticky) {
            // eventInheritance 默认为true
            if (eventInheritance) {
                // Existing sticky events of all subclasses of eventType have to be considered.
                // Note: Iterating over all events may be inefficient with lots of sticky events,
                // thus data structure should be changed to allow a more efficient lookup
                // (e.g. an additional map storing sub classes of super classes: Class -> List<Class>).
                Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
                for (Map.Entry<Class<?>, Object> entry : entries) {
                    Class<?> candidateEventType = entry.getKey();
                    if (eventType.isAssignableFrom(candidateEventType)) {
                        Object stickyEvent = entry.getValue();
                        checkPostStickyEventToSubscription(newSubscription, stickyEvent);
                    }
                }
            } else {
                // 获取粘性事件
                Object stickyEvent = stickyEvents.get(eventType);
                // 处理粘性事件
                checkPostStickyEventToSubscription(newSubscription, stickyEvent);
            }
        }
    }

    private void checkPostStickyEventToSubscription(Subscription newSubscription, Object stickyEvent) {
        if (stickyEvent != null) {
            // If the subscriber is trying to abort the event, it will fail (event is not tracked in posting state)
            // --> Strange corner case, which we don't take care of here.
            postToSubscription(newSubscription, stickyEvent, isMainThread());
        }
    }

    /**
     * Checks if the current thread is running in the main thread.
     * If there is no main thread support (e.g. non-Android), "true" is always returned. In that case MAIN thread
     * subscribers are always called in posting thread, and BACKGROUND subscribers are always called from a background
     * poster.
     */
    private boolean isMainThread() {
        return mainThreadSupport != null ? mainThreadSupport.isMainThread() : true;
    }

    public synchronized boolean isRegistered(Object subscriber) {
        return typesBySubscriber.containsKey(subscriber);
    }

    /**
     * 从 subscriptionsByEventType 中移除订阅对象所有的订阅方法
     *
     * @param subscriber 订阅对象
     * @param eventType  事件类型
     */
    private void unsubscribeByEventType(Object subscriber, Class<?> eventType) {
        // 获取事件类型对应的事件订阅方法列表
        List<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
        if (subscriptions != null) {
            int size = subscriptions.size();
            // 必须用这种方式删除订阅方法，因为每删除一个Subscription，List的长度就会发生变化
            // List的里面的元素的索引也会发生一定的变化
            for (int i = 0; i < size; i++) {
                Subscription subscription = subscriptions.get(i);
                // 如果subscriptions中有订阅对象的方法，那么移除
                if (subscription.subscriber == subscriber) {
                    subscription.active = false;
                    subscriptions.remove(i);
                    i--;
                    size--;
                }
            }
        }
    }

    /**
     * 注销订阅对象，删除订阅对象所有的订阅方法
     */
    public synchronized void unregister(Object subscriber) {
        List<Class<?>> subscribedTypes = typesBySubscriber.get(subscriber);
        if (subscribedTypes != null) {
            // 遍历订阅对象的所有订阅方法，然后将这些方法从 subscriptionsByEventType 中一一移除
            for (Class<?> eventType : subscribedTypes) {
                unsubscribeByEventType(subscriber, eventType);
            }
            // 从 typesBySubscriber移除订阅对象
            typesBySubscriber.remove(subscriber);
        } else {
            // 如果没有register却又调用unregister或者多次调用unregister都会抛出异常
            logger.log(Level.WARNING, "Subscriber to unregister was not registered before: " + subscriber.getClass());
        }
    }

    /**
     * 将事件发送出去，首先会将事件入队，然后再将队列发送出去，这样能保证多个线程调用post()方法也能
     * 高效执行
     */
    public void post(Object event) {
        // 获取当前线程的post状态 postingState
        PostingThreadState postingState = currentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        // 把事件添加到事件队列
        eventQueue.add(event);

        // 判断事件正在post
        if (!postingState.isPosting) {
            postingState.isMainThread = isMainThread();
            postingState.isPosting = true;
            if (postingState.canceled) {
                throw new EventBusException("Internal error. Abort state was not reset");
            }
            try {
                // 遍历事件列表，将所有事件都发送出去
                while (!eventQueue.isEmpty()) {
                    // 该方法会将事件发送出去
                    // eventQueue.remove(0)会移除索引为0的事件，然后会返回这个事件
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }
    }

    /**
     * Called from a subscriber's event handling method, further event delivery will be canceled. Subsequent
     * subscribers
     * won't receive the event. Events are usually canceled by higher priority subscribers (see
     * {@link Subscribe#priority()}). Canceling is restricted to event handling methods running in posting thread
     * {@link ThreadMode#POSTING}.
     */
    public void cancelEventDelivery(Object event) {
        PostingThreadState postingState = currentPostingThreadState.get();
        if (!postingState.isPosting) {
            throw new EventBusException(
                    "This method may only be called from inside event handling methods on the posting thread");
        } else if (event == null) {
            throw new EventBusException("Event may not be null");
        } else if (postingState.event != event) {
            throw new EventBusException("Only the currently handled event may be aborted");
        } else if (postingState.subscription.subscriberMethod.threadMode != ThreadMode.POSTING) {
            throw new EventBusException(" event handlers may only abort the incoming event");
        }

        postingState.canceled = true;
    }

    /**
     * Posts the given event to the event bus and holds on to the event (because it is sticky). The most recent sticky
     * event of an event's type is kept in memory for future access by subscribers using {@link Subscribe#sticky()}.
     */
    public void postSticky(Object event) {
        synchronized (stickyEvents) {
            stickyEvents.put(event.getClass(), event);
        }
        // Should be posted after it is putted, in case the subscriber wants to remove immediately
        post(event);
    }

    /**
     * Gets the most recent sticky event for the given type.
     *
     * @see #postSticky(Object)
     */
    public <T> T getStickyEvent(Class<T> eventType) {
        synchronized (stickyEvents) {
            return eventType.cast(stickyEvents.get(eventType));
        }
    }

    /**
     * Remove and gets the recent sticky event for the given event type.
     *
     * @see #postSticky(Object)
     */
    public <T> T removeStickyEvent(Class<T> eventType) {
        synchronized (stickyEvents) {
            return eventType.cast(stickyEvents.remove(eventType));
        }
    }

    /**
     * Removes the sticky event if it equals to the given event.
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEvent(Object event) {
        synchronized (stickyEvents) {
            Class<?> eventType = event.getClass();
            Object existingEvent = stickyEvents.get(eventType);
            if (event.equals(existingEvent)) {
                stickyEvents.remove(eventType);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Removes all sticky events.
     */
    public void removeAllStickyEvents() {
        synchronized (stickyEvents) {
            stickyEvents.clear();
        }
    }

    public boolean hasSubscriberForEvent(Class<?> eventClass) {
        List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
        if (eventTypes != null) {
            int countTypes = eventTypes.size();
            for (int h = 0; h < countTypes; h++) {
                Class<?> clazz = eventTypes.get(h);
                CopyOnWriteArrayList<Subscription> subscriptions;
                synchronized (this) {
                    subscriptions = subscriptionsByEventType.get(clazz);
                }
                if (subscriptions != null && !subscriptions.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将事件 event 发送出去
     *
     * @param event 事件（即参数）
     */
    private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {
        // 1、获取事件类型
        Class<?> eventClass = event.getClass();
        boolean subscriptionFound = false;
        // 2、eventInheritance表示如果事件有父类或接口，是否执行父类或接口的订阅方法
        // eventInheritance默认true
        if (eventInheritance) {
            // 获取事件的所有父类以及接口
            List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
            int countTypes = eventTypes.size();
            // 遍历所有父类和接口，执行顺序：子类->子类接口->父类->父类接口->父类的父类
            for (int h = 0; h < countTypes; h++) {
                Class<?> clazz = eventTypes.get(h);
                // 将事件对象传递到每一个订阅了该事件的订阅对象以及其父类、接口中
                subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
            }
        } else {
            // 将事件对象传递到每一个订阅了该事件的订阅对象中、不传递到父类和接口
            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
        }

        // 3、如果事件找不到订阅的方法就会抛出异常
        if (!subscriptionFound) {
            if (logNoSubscriberMessages) {
                logger.log(Level.FINE, "No subscribers registered for event " + eventClass);
            }
            if (sendNoSubscriberEvent && eventClass != NoSubscriberEvent.class &&
                    eventClass != SubscriberExceptionEvent.class) {
                post(new NoSubscriberEvent(this, event));
            }
        }
    }

    /**
     * 将事件对象传递给订阅对象
     * 遍历订阅了该事件的所有订阅方法，然后发送给每一个订阅方法
     *
     * @param event        事件
     * @param postingState post状态
     * @param eventClass   事件对象的Class
     * @return 如果该事件有订阅方法就返回true
     */
    private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
        CopyOnWriteArrayList<Subscription> subscriptions;
        // 1、获取所有订阅了该事件类型的所有Subscription（封装了订阅对象、订阅方法）
        synchronized (this) {
            subscriptions = subscriptionsByEventType.get(eventClass);
        }
        if (subscriptions != null && !subscriptions.isEmpty()) {
            // 2、遍历所有Subscription，将事件对象发送到订阅对象中的订阅方法中
            for (Subscription subscription : subscriptions) {
                postingState.event = event;
                postingState.subscription = subscription;
                boolean aborted = false;
                try {
                    // 把事件发送到订阅对象中的订阅方法中
                    postToSubscription(subscription, event, postingState.isMainThread);
                    // 是否被取消
                    aborted = postingState.canceled;
                } finally {
                    postingState.event = null;
                    postingState.subscription = null;
                    postingState.canceled = false;
                }
                // 如果被取消就跳出循环
                if (aborted) {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 把事件发送到订阅对象中的订阅方法中
     *
     * @param subscription 封装了订阅对象和订阅方法的对象
     * @param event        事件
     * @param isMainThread 当前线程是否为主线程
     */
    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        // 根据不同的线程模式执行不同的逻辑
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
                // 执行订阅方法
                invokeSubscriber(subscription, event);
                break;

            case MAIN:
                // 如果已经在主线程中就直接执行订阅方法
                if (isMainThread) {
                    invokeSubscriber(subscription, event);
                } else {
                    // 把事件排队交给主线程执行
                    /**{@link HandlerPoster */
                    mainThreadPoster.enqueue(subscription, event);
                }
                break;

            case MAIN_ORDERED:
                if (mainThreadPoster != null) {
                    // 把事件排队交给主线程处理
                    mainThreadPoster.enqueue(subscription, event);
                } else {
                    // temporary: technically not correct as poster not decoupled from subscriber
                    invokeSubscriber(subscription, event);
                }
                break;
            case BACKGROUND:
                // 如果当前线程是在主线程则切换到子线程中执行
                if (isMainThread) {
                    // backgroundPoster使用了 CachedThreadPool线程池，这里会把事件排队交给线程池处理
                    backgroundPoster.enqueue(subscription, event);
                } else {
                    invokeSubscriber(subscription, event);
                }
                break;

            case ASYNC:
                // 不管当前线程是什么都切换到子线程中执行
                // asyncPoster也是使用了 CachedThreadPool线程池，这里会把事件排队交给线程池处理
                asyncPoster.enqueue(subscription, event);
                break;

            default:
                throw new IllegalStateException("Unknown thread mode: " + subscription.subscriberMethod.threadMode);
        }
    }

    /**
     * 这个方法会检测post的事件对象是否有父类以及接口，如果有，那么在post的时候也会将事件对象传递
     * 到父类以及接口中的订阅方法中
     *
     * @param eventClass post传入的参数的Class，也就是事件的Class
     */
    private static List<Class<?>> lookupAllEventTypes(Class<?> eventClass) {
        synchronized (eventTypesCache) {
            // 1、获取事件对象的所有父类以及接口
            List<Class<?>> eventTypes = eventTypesCache.get(eventClass);
            // 2、如果为空，就表明这个事件对象的所有父类和接口还没有缓存
            if (eventTypes == null) {
                eventTypes = new ArrayList<>();
                Class<?> clazz = eventClass;
                // 遍历添加事件的所有父类，以及父类实现的接口到 eventTypes
                while (clazz != null) {
                    // 将事件对象的自身Class先添加到集合中
                    // 该事件对象对应的所有父类以及接口的集合中，第一个元素是事件对象自身的Class
                    eventTypes.add(clazz);
                    // 添加事件对象的所有父类、接口
                    addInterfaces(eventTypes, clazz.getInterfaces());
                    // 获取父类
                    clazz = clazz.getSuperclass();
                }
                eventTypesCache.put(eventClass, eventTypes);
            }
            return eventTypes;
        }
    }

    /**
     * 把所有接口都添加到 eventTypes中
     */
    static void addInterfaces(List<Class<?>> eventTypes, Class<?>[] interfaces) {
        for (Class<?> interfaceClass : interfaces) {
            if (!eventTypes.contains(interfaceClass)) {
                eventTypes.add(interfaceClass);
                addInterfaces(eventTypes, interfaceClass.getInterfaces());
            }
        }
    }

    /**
     * Invokes the subscriber if the subscriptions is still active. Skipping subscriptions prevents race conditions
     * between {@link #unregister(Object)} and event delivery. Otherwise the event might be delivered after the
     * subscriber unregistered. This is particularly important for main thread delivery and registrations bound to the
     * live cycle of an Activity or Fragment.
     */
    void invokeSubscriber(PendingPost pendingPost) {
        Object event = pendingPost.event;
        Subscription subscription = pendingPost.subscription;
        PendingPost.releasePendingPost(pendingPost);
        if (subscription.active) {
            invokeSubscriber(subscription, event);
        }
    }

    /**
     * 执行订阅方法
     *
     * @param subscription 订阅方法的一个封装对象
     * @param event        事件
     */
    void invokeSubscriber(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (InvocationTargetException e) {
            handleSubscriberException(subscription, event, e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    private void handleSubscriberException(Subscription subscription, Object event, Throwable cause) {
        if (event instanceof SubscriberExceptionEvent) {
            if (logSubscriberExceptions) {
                // Don't send another SubscriberExceptionEvent to avoid infinite event recursion, just log
                logger.log(Level.SEVERE, "SubscriberExceptionEvent subscriber " + subscription.subscriber.getClass()
                        + " threw an exception", cause);
                SubscriberExceptionEvent exEvent = (SubscriberExceptionEvent) event;
                logger.log(Level.SEVERE, "Initial event " + exEvent.causingEvent + " caused exception in "
                        + exEvent.causingSubscriber, exEvent.throwable);
            }
        } else {
            if (throwSubscriberException) {
                throw new EventBusException("Invoking subscriber failed", cause);
            }
            if (logSubscriberExceptions) {
                logger.log(Level.SEVERE, "Could not dispatch event: " + event.getClass() + " to subscribing class "
                        + subscription.subscriber.getClass(), cause);
            }
            if (sendSubscriberExceptionEvent) {
                SubscriberExceptionEvent exEvent = new SubscriberExceptionEvent(this, cause, event,
                        subscription.subscriber);
                post(exEvent);
            }
        }
    }

    /**
     * 这个表示的是当前正在post的事件，以及事件的状态，这个类在每一个线程都有
     * 一个副本，不同线程之间的副本互不干涉
     */
    final static class PostingThreadState {
        /**
         * 存储的是post的事件，如String，Message
         */
        final List<Object> eventQueue = new ArrayList<>();
        /**
         * 标志是否在posting
         */
        boolean isPosting;
        boolean isMainThread;
        Subscription subscription;
        Object event;
        boolean canceled;
    }

    ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * For internal use only.
     */
    public Logger getLogger() {
        return logger;
    }

    // Just an idea: we could provide a callback to post() to be notified, an alternative would be events, of course...
    /* public */interface PostCallback {
        void onPostCompleted(List<SubscriberExceptionEvent> exceptionEvents);
    }

    @Override
    public String toString() {
        return "EventBus[indexCount=" + indexCount + ", eventInheritance=" + eventInheritance + "]";
    }
}
