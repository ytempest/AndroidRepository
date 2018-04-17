package com.ytempest.smartevent;

/**
 * @author ytempest
 *         Description：
 */
public class Subscription {
    Object subscriber;
    SubscriberMethod subscriberMethod;

    Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
    }
}
