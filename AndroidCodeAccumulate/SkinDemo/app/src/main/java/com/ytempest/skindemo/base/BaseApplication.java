package com.ytempest.skindemo.base;

import android.app.Application;

import com.ytempest.skin.skin.SkinManager;


/**
 * @author ytempest
 *         Description：
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.getInstance().init(this);
    }
}
