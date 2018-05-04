package com.ytempest.okhttpanalysis.sample3.http;

/**
 * @author ytempest
 *         Description：
 */
public abstract class NamedRunnable implements Runnable {

    @Override
    public void run() {
        execute();
    }

    public abstract void execute();
}
