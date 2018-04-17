package com.ytempest.smartevent.post;

/**
 * @author ytempest
 *         Description：这是一个事件处理的队列
 */
public class PendingPostQueue {
    private PendingPost head;
    private PendingPost tail;


    synchronized void enqueue(PendingPost pendingPost) {
        if (pendingPost == null) {
            throw new NullPointerException("null cannot be enqueued");
        }

        if (tail != null) {
            tail.next = pendingPost;
            tail = pendingPost;
        } else if (head == null) {
            head = tail = pendingPost;
        } else {
            throw new IllegalStateException("the queue have head, but no tail");
        }

        notifyAll();
    }

    synchronized PendingPost poll() {
        PendingPost pendingPost = head;
        if (head != null) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
        }
        return pendingPost;
    }

    synchronized PendingPost poll(int maxMilliToWait) throws InterruptedException {
        if (head == null) {
            wait(maxMilliToWait);
        }
        return poll();
    }
}
