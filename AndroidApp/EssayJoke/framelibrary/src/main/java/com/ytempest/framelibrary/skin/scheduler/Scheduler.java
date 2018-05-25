package com.ytempest.framelibrary.skin.scheduler;

/**
 * @author ytempest
 *         Description：规范线程执行的一个规范
 */
public interface Scheduler {
    void schedule(Runnable task);
}
