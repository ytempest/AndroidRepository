package com.ytempest.ndkdemo.util;

/**
 * @author ytempest
 *         Description：
 */
public class PosixThread {
    static {
        System.loadLibrary("posix_thread");
    }
    public native void thread();
}
