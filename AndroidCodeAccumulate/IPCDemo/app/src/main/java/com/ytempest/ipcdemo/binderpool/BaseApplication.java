package com.ytempest.ipcdemo.binderpool;

import android.app.Application;

import com.ytempest.ipcdemo.binderpool.service.BinderPool;

/**
 * @author ytempest
 *         Description：
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        BinderPool binderPool = BinderPool.getInstance(this);
    }
}
