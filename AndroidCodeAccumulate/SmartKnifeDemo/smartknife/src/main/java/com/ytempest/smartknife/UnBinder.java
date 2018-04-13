package com.ytempest.smartknife;

import android.support.annotation.UiThread;

/**
 * @author ytempest
 *         Description：
 */
public interface UnBinder {
    @UiThread
    void unbind();

    UnBinder EMPTY = new UnBinder() {
        @Override
        public void unbind() {
        }
    };
}
