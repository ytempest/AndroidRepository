/*
 * Copyright (C) 2012-2016 Markus Junginger, greenrobot (http://greenrobot.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.greenrobot.eventbus;

final class PendingPostQueue {
    /**
     * 这里使用链表的方式实现队列
     */
    private PendingPost head;
    private PendingPost tail;

    /**
     * 同一个时间只允许一个线程把对象送进队列
     */
    synchronized void enqueue(PendingPost pendingPost) {
        if (pendingPost == null) {
            throw new NullPointerException("null cannot be enqueued");
        }
        if (tail != null) {
            // 把 pendingPost放进队列
            tail.next = pendingPost;
            // 把队尾的指针指向pendingPost
            tail = pendingPost;
        } else if (head == null) {
            // 如果队首指针为空，则表明队列为空
            head = tail = pendingPost;
        } else {
            // 如果走这个分支，则表明队列不为空，队首指针指向队首，但是队尾指针没有指向任何对象
            // 这种情况下可能是程序异常
            throw new IllegalStateException("Head present, but no tail");
        }
        // 唤醒所有线程，如果有线程会对队列进行处理，那么唤醒线程就会处理队列里面的事件
        notifyAll();
    }

    /**
     * 将队首元素出队
     */
    synchronized PendingPost poll() {
        // 获取队首元素
        PendingPost pendingPost = head;
        // 将队首指针指向下一个队列元素
        if (head != null) {
            head = head.next;
            // 如果下一个队列元素就是最后一个元素，那么就将队尾指针置null
            if (head == null) {
                tail = null;
            }
        }
        return pendingPost;
    }

    synchronized PendingPost poll(int maxMillisToWait) throws InterruptedException {
        // 如果队列没有数据，那么就等待 maxMillisToWait 毫秒
        // 即：可能程序读取队列的数据过快，把队列的数据都读取完了，但数据进队的速度比较慢，
        // 所以等待一会再做处理，这样就可以适当更上处理的速度，而且不会阻塞线程
        if (head == null) {
            wait(maxMillisToWait);
        }
        return poll();
    }

}
