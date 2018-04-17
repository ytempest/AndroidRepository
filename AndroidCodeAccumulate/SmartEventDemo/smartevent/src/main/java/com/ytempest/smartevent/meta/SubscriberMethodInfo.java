package com.ytempest.smartevent.meta;

import com.ytempest.smartevent.ThreadMode;

/**
 * @author ytempest
 *         Description：这是索引用于封装订阅方法的订阅信息的一个封装类
 */
public class SubscriberMethodInfo {
    final String methodName;

    final Class<?> eventClass;

    final ThreadMode threadMode;

    final int priority;

    public SubscriberMethodInfo(String methodName, Class<?> eventClass, ThreadMode threadMode, int priority) {
        this.methodName = methodName;
        this.eventClass = eventClass;
        this.threadMode = threadMode;
        this.priority = priority;
    }
}
