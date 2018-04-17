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
        EventBus.builder().ignoreGeneratedIndex(true).addIndex(new MyEventBusIndex()).installDefaultEventBus();

    }
}
