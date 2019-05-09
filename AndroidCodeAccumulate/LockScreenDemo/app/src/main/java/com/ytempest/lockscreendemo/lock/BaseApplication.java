package com.ytempest.lockscreendemo.lock;

import android.app.Application;

/**
 * @author ytempest
 * @date 2019/5/8
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DeviceAdminTool.getInstance().init(this);
    }
}
