package com.ytempest.okhttpanalysis.my_okhttp.http;

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
