package com.ytempest.smartevent;

import android.os.Looper;
import android.util.Log;

import com.ytempest.smartevent.post.AsyncPoster;
import com.ytempest.smartevent.post.BackgroundPoster;
import com.ytempest.smartevent.post.HandlerPoster;
import com.ytempest.smartevent.post.PendingPost;
import com.ytempest.smartevent.post.Poster;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author ytempest
 *         Description：
 */
public class SmartEvent {

    private final static String TAG = "SmartEvent";

    private final ExecutorService mExecutorService;

    static volatile SmartEvent mDefaultInstance;
    private static final SmartEventBuilder DEFAULT_BUILDER = new SmartEventBuilder();
    /**
     * 存储每一个事件类型所对应的所有方法的Map
     * key：事件类型对象（即：标记了Subscribe注解方法的参数）
     * value：事件类型对象对应的所有订阅方法（即参数都是事件类型（如String，Message）的方法）
     */
    private final HashMap<Class<?>, CopyOnWriteArrayList<Subscription>> mEventTypeWithSubscription;

    private static final HashMap<Class<?>, List<Class<?>>> EVENTTYPE_INHERITANCE_CACHES = new HashMap<>();

    /**
     * 存储了每一个订阅对象的所有方法信息
     * key：订阅对象（如MainActivity）
     * value：订阅对象中所有标记了Subscribe注解的方法的参数列表
     */
    private final HashMap<Object, List<Class<?>>> mSubscriberWithEvenType;

    private final AsyncPoster mAsyncPoster;
    private final BackgroundPoster mBackground;
    private final Poster mHandlerPoster;
    private final boolean isEventInheritance;
    private SubscribeMethodFinder mSubscribeMethodFinder;

    /**
     * 保证每一个线程有且只有一个 PostingThreadState对象的副本，一个线程的无法对另一个线程的
     * PostingThreadState对象进行修改；从而保证 PostingThreadState对象的线程安全
     */
    private final ThreadLocal<PostingThreadState> mCurrentPostingThreadState = new ThreadLocal<PostingThreadState>() {
        @Override
        protected PostingThreadState initialValue() {
            return new PostingThreadState();
        }
    };

    private SmartEvent() {
        this(DEFAULT_BUILDER);
    }

    SmartEvent(SmartEventBuilder builder) {
        this.mEventTypeWithSubscription = new HashMap<>();
        this.mSubscriberWithEvenType = new HashMap<>();
        mSubscribeMethodFinder = new SubscribeMethodFinder(builder.isIgnoreGeneratedIndex, builder.mSubscriberInfoIndexes);
        mExecutorService = builder.mExecutorService;
        mAsyncPoster = new AsyncPoster(this);
        mBackground = new BackgroundPoster(this);
        mHandlerPoster = new HandlerPoster(this, Looper.getMainLooper(), 10);
        isEventInheritance = builder.isEventInheritance;
    }

    public static SmartEventBuilder builder() {
        return new SmartEventBuilder();
    }

    public static SmartEvent getDefault() {
        if (mDefaultInstance == null) {
            synchronized (SmartEvent.class) {
                if (mDefaultInstance == null) {
                    mDefaultInstance = new SmartEvent();
                }
            }
        }
        return mDefaultInstance;
    }

    public void register(Object subscriber) {
        // 1、获取订阅对象中所有的订阅方法
        Class<?> subscriberClass = subscriber.getClass();
        List<SubscriberMethod> subscriberMethods = mSubscribeMethodFinder.findSubscriberMethods(subscriberClass);

        // 2、将所有的订阅方法分别添加到 mSubscriberWithEvenType 和 mEventTypeWithSubscription 中
        synchronized (this) {
            for (SubscriberMethod subscriberMethod : subscriberMethods) {
                subscribe(subscriber, subscriberMethod);
            }
        }
    }

    private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
        Class<?> eventTypeClass = subscriberMethod.eventType;

        // 1、将订阅方法添加到 mSubscriberWithEvenType
        List<Class<?>> eventTypes = mSubscriberWithEvenType.get(subscriber);
        if (eventTypes == null) {
            eventTypes = new ArrayList<>();
            mSubscriberWithEvenType.put(subscriber, eventTypes);
        }
        eventTypes.add(eventTypeClass);


        // 2、将订阅方法添加到 mEventTypeWithSubscription
        CopyOnWriteArrayList<Subscription> subscriptions = mEventTypeWithSubscription.get(subscriberMethod.eventType);
        Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
        if (subscriptions == null) {
            subscriptions = new CopyOnWriteArrayList<>();
            mEventTypeWithSubscription.put(eventTypeClass, subscriptions);
        } else {
            // 如果不为空，则说明已经为订阅对象注册过了，即register两次
            if (subscriptions.contains(newSubscription)) {
                throw new SmartEventException("Subscriber " + subscriber.getClass() + " already registered to event "
                        + eventTypeClass);
            }
        }

        // 按照订阅方法的优先级先后添加到列表
        int size = subscriptions.size();
        for (int i = 0; i <= size; i++) {
            if (i == size || subscriptions.get(i).subscriberMethod.priority
                    < newSubscription.subscriberMethod.priority) {
                subscriptions.add(i, newSubscription);
                break;
            }
        }

    }

    public void unregister(Object subscriber) {
        List<Class<?>> eventTypes = mSubscriberWithEvenType.get(subscriber);
        for (Class<?> eventType : eventTypes) {
            // 从 mEventTypeWithSubscription 移除订阅对象中的所有订阅方法
            removeSubscriberMethodByEventType(subscriber, eventType);
        }

        // 移除订阅对象
        mSubscriberWithEvenType.remove(subscriber);
    }


    /**
     * 从 mEventTypeWithSubscription 移除订阅对象中的所有订阅方法
     */
    private void removeSubscriberMethodByEventType(Object subscriber, Class<?> eventType) {
        List<Subscription> subscriptions = mEventTypeWithSubscription.get(eventType);

        if (subscriptions != null) {
            int size = subscriptions.size();
            // 必须用这种方式删除订阅方法，因为每删除一个Subscription，List的长度就会发生变化
            // List的里面的元素的索引也会发生一定的变化
            for (int i = 0; i < size; i++) {
                Subscription subscription = subscriptions.get(i);
                if (subscriber == subscription.subscriber) {
                    subscriptions.remove(i);
                    i--;
                    size--;
                }
            }

            // 如果说当前事件的订阅方法数为0，那么移除这一个事件
            if (subscriptions.size() == 0) {
                mEventTypeWithSubscription.remove(eventType);
            }
        }
    }

    public void post(Object event) {
        // 获取当前线程的post状态 postingState
        PostingThreadState postingState = mCurrentPostingThreadState.get();
        List<Object> eventQueue = postingState.eventQueue;
        eventQueue.add(event);

        if (!postingState.isPosting) {
            // 设置否为主线程
            postingState.isMainThread = isMainThread();
            // 设置状态为正在post
            postingState.isPosting = true;
            try {
                while (!eventQueue.isEmpty()) {
                    //eventQueue.remove(0)会移除索引为0的事件，然后会返回这个事件
                    postSingleEvent(eventQueue.remove(0), postingState);
                }
            } finally {
                // 当事件都发送出去后立即重置一些参数
                postingState.isPosting = false;
                postingState.isMainThread = false;
            }
        }

    }

    /**
     * 遍历该事件的订阅方法，然后把事件post给每一个订阅方法
     */
    private void postSingleEvent(Object event, PostingThreadState postingState) {
        Class<?> eventClass = event.getClass();
        boolean subscriptionFound = false;

        if (isEventInheritance) {
            List<Class<?>> eventTypes = lookupAllEventTypes(eventClass);
            for (Class<?> eventType : eventTypes) {
                subscriptionFound |= postSingleEventForEventType(event, postingState, eventType);
            }
        } else {
            subscriptionFound = postSingleEventForEventType(event, postingState, eventClass);
        }

        if (!subscriptionFound) {
            Log.e(TAG, "No subscribers registered for event:" + eventClass);
        }
    }

    private static List<Class<?>> lookupAllEventTypes(Class<?> eventClass) {
        List<Class<?>> eventTypes = EVENTTYPE_INHERITANCE_CACHES.get(eventClass);
        synchronized (EVENTTYPE_INHERITANCE_CACHES) {
            if (eventTypes == null) {
                eventTypes = new ArrayList<>();
                Class<?> clazz = eventClass;
                while (clazz != null) {
                    eventTypes.add(clazz);
                    addInterfaces(eventTypes, clazz.getInterfaces());
                    clazz = clazz.getSuperclass();
                }
                EVENTTYPE_INHERITANCE_CACHES.put(eventClass, eventTypes);
            }
        }

        return eventTypes;
    }

    private static void addInterfaces(List<Class<?>> eventTypes, Class<?>[] interfaces) {
        for (Class<?> interfaceClass : interfaces) {
            if (!eventTypes.contains(interfaceClass)) {
                eventTypes.add(interfaceClass);
                addInterfaces(eventTypes, interfaceClass.getInterfaces());
            }
        }
    }

    private boolean postSingleEventForEventType(Object event, PostingThreadState postingState, Class<?> eventClass) {
        CopyOnWriteArrayList<Subscription> subscriptions;
        synchronized (this) {
            subscriptions = mEventTypeWithSubscription.get(eventClass);
        }

        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                // 把post状态指向要处理的事件
                postingState.subscription = subscription;
                postingState.event = event;
                try {
                    postToSubscription(subscription, event, postingState.isMainThread);
                } finally {
                    // 当事件被处理完毕后就重置post状态
                    postingState.subscription = null;
                    postingState.event = null;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 在订阅方法指定的线程模式中处理事件
     */
    private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        // 根据不同的线程模式执行不同的逻辑
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
                // 在方法的原线程中执行
                invokeSubscriber(subscription, event);
                break;

            case MAIN:
                if (isMainThread) {
                    invokeSubscriber(subscription, event);
                } else {
                    // 切换到主线程中执行
                    mHandlerPoster.enqueue(subscription, event);
                }
                break;

            case BACKGROUND:
                if (isMainThread) {
                    // 如果是在主线程就开一个子线程执行
                    mBackground.enqueue(subscription, event);
                } else {
                    // 不是在主线程则直接在子线程中执行
                    invokeSubscriber(subscription, event);
                }
                break;

            case ASYNC:
                // 开启一个新的线程执行方法
                mAsyncPoster.enqueue(subscription, event);
                break;

            default:
                break;
        }
    }


    private void invokeSubscriber(Subscription subscription, Object event) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "invokeSubscriber: you can't access UI in this thread", e.getCause());
            e.printStackTrace();
        }
    }

    public void invokeSubscriber(PendingPost pendingPost) {
        Subscription subscription = pendingPost.subscription;
        Object event = pendingPost.event;
        PendingPost.releasePendingPost(pendingPost);
        invokeSubscriber(subscription, event);
    }

    /**
     * 清除缓存，主要用于单元测试的时候时候
     */
    public static void clearCaches() {
        SubscribeMethodFinder.clearCaches();
        EVENTTYPE_INHERITANCE_CACHES.clear();
    }

    /**
     * 判断当前线程是否主线程
     */
    private boolean isMainThread() {
        Looper mainLooper = Looper.getMainLooper();
        return mainLooper != null && mainLooper == Looper.myLooper();
    }

    /**
     * 获取 SmartEvent处理事件的线程池
     */
    public ExecutorService getExecutorService() {
        return mExecutorService;
    }

    /**
     * @author ytempest
     *         Description：这是当前正在post的状态信息
     */
    private final static class PostingThreadState {
        /**
         * 存储的是post的事件，如String，Message
         */
        final List<Object> eventQueue = new ArrayList<>();
        /**
         * 标志是否在posting，默认为false
         */
        boolean isPosting;
        boolean isMainThread;
        Subscription subscription;
        Object event;
    }


}
