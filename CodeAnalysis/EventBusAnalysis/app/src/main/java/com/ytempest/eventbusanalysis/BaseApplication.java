package com.ytempest.eventbusanalysis;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbusperf.MyEventBusIndex;

/**
 * @author ytempest
 *         Description：
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // ignoreGeneratedIndex()：如果为true则表示忽略注解生成器生成的MyEventBusIndex
        // addIndex()：添加注解生成器生成的索引类
        // installDefaultEventBus()：安装默认配置的EventBus
        EventBus.builder().ignoreGeneratedIndex(false).addIndex(new MyEventBusIndex()).installDefaultEventBus();
    }
}
