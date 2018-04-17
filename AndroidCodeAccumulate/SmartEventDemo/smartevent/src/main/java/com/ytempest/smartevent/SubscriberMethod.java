package com.ytempest.smartevent;

import java.lang.reflect.Method;

/**
 * @author ytempest
 *         Description：这是一个订阅方法的封装类
 */
public class SubscriberMethod {
    Method method;
    Class<?> eventType;
    ThreadMode threadMode;
    int priority;

    public SubscriberMethod(Method method, Class<?> eventType, ThreadMode threadMode, int priority) {
        this.method = method;
        this.eventType = eventType;
        this.threadMode = threadMode;
        this.priority = priority;
    }
}
