package com.ytempest.rxjavaanalysis.rxjava.observable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class SimpleQueue<T> {

    private List<T> queue;

    public SimpleQueue() {
        queue = new ArrayList<>();
    }

    public synchronized void offer(T value) {
        if (value != null) {
            queue.add(queue.size(), value);
        }
    }

    public synchronized T poll() {
        if (!isEmpty()) {
            T value = queue.get(0);
            queue.remove(0);
            return value;
        }
        return null;
    }

    public boolean isEmpty() {
        return queue == null || queue.size() == 0;
    }

    public void clear() {
        queue.clear();
    }
}
