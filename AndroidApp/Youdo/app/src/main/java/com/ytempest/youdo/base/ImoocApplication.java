package com.ytempest.youdo.base;

import android.app.Application;

import com.ytempest.framelibrary.skin.SkinManager;

/**
 * @author ytempest
 *         Description：
 */
public class ImoocApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SkinManager.getInstance().init(this);
    }
}
