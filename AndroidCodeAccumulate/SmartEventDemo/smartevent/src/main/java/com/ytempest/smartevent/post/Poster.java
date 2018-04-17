package com.ytempest.smartevent.post;

import com.ytempest.smartevent.Subscription;

/**
 * @author ytempest
 *         Description：定义了事件进入队列的规范
 */
public interface Poster {
    void enqueue(Subscription subscription, Object event);
}
