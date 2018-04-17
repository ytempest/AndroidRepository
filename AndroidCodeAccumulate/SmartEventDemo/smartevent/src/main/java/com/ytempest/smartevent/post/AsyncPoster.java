package com.ytempest.smartevent.post;

import com.ytempest.smartevent.SmartEvent;
import com.ytempest.smartevent.Subscription;

/**
 * @author ytempest
 *         Description：
 */
public class AsyncPoster implements Runnable, Poster {

    private PendingPostQueue mQueue;
    private SmartEvent mSmartEvent;

    public AsyncPoster(SmartEvent smartEvent) {
        mSmartEvent = smartEvent;
        mQueue = new PendingPostQueue();
    }

    @Override
    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        mQueue.enqueue(pendingPost);
        mSmartEvent.getExecutorService().execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = mQueue.poll();
        if (pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        mSmartEvent.invokeSubscriber(pendingPost);
    }
}
