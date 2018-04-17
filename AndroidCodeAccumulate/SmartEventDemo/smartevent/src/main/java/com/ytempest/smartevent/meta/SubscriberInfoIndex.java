package com.ytempest.smartevent.meta;

/**
 * @author ytempest
 *         Description：规范从索引类中获取订阅信息的方法
 */
public interface SubscriberInfoIndex {
    /**
     * 根据订阅对象从索引类中获取其所有订阅方法
     *
     * @param subscriberClass 订阅对象的Class
     * @return 封装了订阅对象以及其所有方法的一个 SubscriberInfo对象
     */
    SubscriberInfo getSubscriberInfo(Class<?> subscriberClass);
}
