package com.ytempest.smartevent.meta;

import com.ytempest.smartevent.SubscriberMethod;

/**
 * @author ytempest
 *         Description：定义了从索引封装类中获取订阅对象和订阅方法的规范
 */
public interface SubscriberInfo {
    /**
     * 获取订阅对象
     */
    Class<?> getSubscriberClass();

    /**
     * 获取当前订阅对象在索引类中的订阅方法
     */
    SubscriberMethod[] getSubscriberMethod();

}
