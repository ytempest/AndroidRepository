package com.ytempest.smartevent.post;

import com.ytempest.smartevent.Subscription;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：事件队列的元素，封装了事件的订阅方法和事件的类型，这是线程池处理事件的一个单元
 */
public class PendingPost {

    /**
     * 对象池，减少开辟内存的次数
     */
    private static List<PendingPost> PENDING_POST_POOL = new ArrayList<>();
    private static final int MAX_PENDING_POST_COUNT = 5000;

    public Subscription subscription;
    public Object event;
    public PendingPost next;

    private PendingPost(Subscription subscription, Object event) {
        this.subscription = subscription;
        this.event = event;
    }

    /**
     * 获取PendingPost
     */
    public static PendingPost obtainPendingPost(Subscription subscription, Object event) {
        synchronized (PENDING_POST_POOL) {
            int size = PENDING_POST_POOL.size();
            if (size > 0) {
                PendingPost pendingPost = PENDING_POST_POOL.remove(size - 1);
                pendingPost.subscription = subscription;
                pendingPost.event = event;
                pendingPost.next = null;
                return pendingPost;
            }
        }

        return new PendingPost(subscription, event);
    }

    /**
     * 回收PendingPost
     */
    public static void releasePendingPost(PendingPost pendingPost) {
        pendingPost.subscription = null;
        pendingPost.event = null;
        pendingPost.next = null;
        synchronized (PENDING_POST_POOL) {
            if (PENDING_POST_POOL.size() < MAX_PENDING_POST_COUNT) {
                PENDING_POST_POOL.add(pendingPost);
            }
        }
    }

}
