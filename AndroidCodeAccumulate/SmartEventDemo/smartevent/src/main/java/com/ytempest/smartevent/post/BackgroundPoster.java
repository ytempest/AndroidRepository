package com.ytempest.smartevent.post;

import android.util.Log;

import com.ytempest.smartevent.SmartEvent;
import com.ytempest.smartevent.Subscription;

/**
 * @author ytempest
 *         Description：
 */
public class BackgroundPoster implements Runnable, Poster {

    private static final String TAG = "BackgroundPoster";

    private PendingPostQueue mQueue;
    private SmartEvent mSmartEvent;
    private volatile boolean isExecutorRunning = false;

    public BackgroundPoster(SmartEvent smartEvent) {
        mSmartEvent = smartEvent;
        mQueue = new PendingPostQueue();
    }

    @Override
    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this) {
            mQueue.enqueue(pendingPost);
            if (!isExecutorRunning) {
                mSmartEvent.getExecutorService().execute(this);
                isExecutorRunning = true;
            }
        }
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    PendingPost pendingPost = mQueue.poll(1000);
                    if (pendingPost == null) {
                        synchronized (this) {
                            pendingPost = mQueue.poll();
                            if (pendingPost == null) {
                                isExecutorRunning = false;
                                return;
                            }
                        }
                    }
                    mSmartEvent.invokeSubscriber(pendingPost);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "run: " + Thread.currentThread().getName() + "was interrupted");
            }
        } finally {
            isExecutorRunning = false;
        }
    }
}
