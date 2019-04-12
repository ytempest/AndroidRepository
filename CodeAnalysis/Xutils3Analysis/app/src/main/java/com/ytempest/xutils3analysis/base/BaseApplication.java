package com.ytempest.xutils3analysis.base;

import android.app.Application;

import com.ytempest.xutils3analysis.BuildConfig;

import org.xutils.x;

/**
 * @author ytempest
 * @date 2019/4/12
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
    }
}
