package com.ytempest.smartevent.post;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.ytempest.smartevent.SmartEvent;
import com.ytempest.smartevent.Subscription;


/**
 * @author ytempest
 *         Description：
 */
public class HandlerPoster extends Handler implements Poster {

    private PendingPostQueue mQueue;
    private SmartEvent mSmartEvent;
    private boolean isHandlerActive = false;
    /**
     * 处理事件允许的最大时间，单位毫秒，如果处理事件的时间超出这个时间，那么就会发送消息
     * 给Handler，重新从队列拉取事件进行处理，因为在这个时间内可能会有新的事件入队
     * 问题：为什么这个handler不直接等待事件到来，然后直接处理，反而是发送消息？
     * 答：因为这是主线程，主线程不能阻塞，如果处理完一个事件后，而在这个处理过程中，新的事件
     * 到来，那么只能通过消息发送Handler，让Handler重新处理
     */
    private int maxMillisInsideHandleMessage;

    public HandlerPoster(SmartEvent smartEvent, Looper looper, int maxMillisInsideHandleMessage) {
        super(looper);
        mSmartEvent = smartEvent;
        this.maxMillisInsideHandleMessage = maxMillisInsideHandleMessage;
        mQueue = new PendingPostQueue();
    }

    @Override
    public void enqueue(Subscription subscription, Object event) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(subscription, event);
        synchronized (this) {
            mQueue.enqueue(pendingPost);
            if (!isHandlerActive) {
                isHandlerActive = true;
                if (!sendMessage(obtainMessage())) {
                    throw new IllegalStateException("Could not send Handler message");
                }
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        // 是否重新发送了消息
        boolean reScheduled = false;
        try {
            long startTime = SystemClock.currentThreadTimeMillis();
            while (true) {
                PendingPost pendingPost = mQueue.poll();
                if (pendingPost == null) {
                    synchronized (this) {
                        pendingPost = mQueue.poll();
                        if (pendingPost == null) {
                            isHandlerActive = false;
                            return;
                        }
                    }
                }
                mSmartEvent.invokeSubscriber(pendingPost);
                // 获取方法执行的时间
                long timeInMethod = SystemClock.currentThreadTimeMillis() - startTime;
                if (timeInMethod >= maxMillisInsideHandleMessage) {
                    if (!sendMessage(obtainMessage())) {
                        throw new IllegalStateException("Could not send handler message");
                    }
                    reScheduled = true;
                    return;
                }
            }
        } finally {
            isHandlerActive = reScheduled;
        }
    }
}
