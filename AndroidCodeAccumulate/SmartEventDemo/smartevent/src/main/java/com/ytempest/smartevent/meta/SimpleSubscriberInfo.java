package com.ytempest.smartevent.meta;

import com.ytempest.smartevent.SubscriberMethod;
import com.ytempest.smartevent.ThreadMode;

import java.lang.reflect.Method;

/**
 * @author ytempest
 *         Description：一个封装索引的订阅对象以及其订阅方法的一个类，一个 SimpleSubscriberInfo 对
 *         应一个 订阅对象的订阅信息
 */
public class SimpleSubscriberInfo implements SubscriberInfo {

    private final Class<?> subscriber;

    private final boolean shouldCheckSuperclass;

    private final SubscriberMethodInfo[] subscriberMethodInfos;

    public SimpleSubscriberInfo(Class<?> subscriber, boolean shouldCheckSuperclass, SubscriberMethodInfo[] subscriberMethodInfos) {
        this.subscriber = subscriber;
        this.shouldCheckSuperclass = shouldCheckSuperclass;
        this.subscriberMethodInfos = subscriberMethodInfos;
    }


    @Override
    public Class<?> getSubscriberClass() {
        return subscriber;
    }

    @Override
    public SubscriberMethod[] getSubscriberMethod() {
        int methodLength = subscriberMethodInfos.length;
        SubscriberMethod[] subscriberMethods = new SubscriberMethod[methodLength];
        SubscriberMethodInfo methodInfo = null;
        for (int i = 0; i < methodLength; i++) {
            methodInfo = subscriberMethodInfos[i];

            subscriberMethods[i] = createSubscriberMethod(methodInfo.methodName,
                    methodInfo.eventClass, methodInfo.threadMode, methodInfo.priority);
        }
        return subscriberMethods;
    }

    /**
     * 创建一个 SubscriberMethod 对象
     */
    private SubscriberMethod createSubscriberMethod(String methodName, Class<?> eventClass, ThreadMode threadMode, int priority) {
        Method subscriberMethod = null;
        try {
            subscriberMethod = subscriber.getMethod(methodName, eventClass);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return new SubscriberMethod(subscriberMethod, eventClass, threadMode, priority);
    }

}
