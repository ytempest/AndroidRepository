package com.ytempest.studentmanage.thread;

import android.os.Handler;
import android.os.Looper;

/**
 * @author ytempest
 *         Description：
 */
public class MainThreadExecutor {
    private static Handler handler = null;

    static {
        handler = new Handler(Looper.getMainLooper());
    }

    private MainThreadExecutor() {
    }

    public static void execute(Runnable runnable) {
        handler.post(runnable);
    }

    public static void execute(Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }
}
