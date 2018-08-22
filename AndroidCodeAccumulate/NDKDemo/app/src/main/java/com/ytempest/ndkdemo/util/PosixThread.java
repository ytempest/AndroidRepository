package com.ytempest.ndkdemo.util;

/**
 * @author ytempest
 *         Description：
 */
public class PosixThread {
    static {
        System.loadLibrary("posix_thread");
    }

    public native void init();

    public native void thread();

    public native void destroy();
}
