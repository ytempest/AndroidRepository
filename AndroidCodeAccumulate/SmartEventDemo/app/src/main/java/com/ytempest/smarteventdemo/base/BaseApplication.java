package com.ytempest.smarteventdemo.base;

import android.app.Application;

import com.ytempest.smartevent.SmartEvent;

/**
 * @author ytempest
 *         Description：
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SmartEvent.builder()
          //    .addSubscriberInfoIndex(new SmartEventIndex())
                .installDefaultSmartEvent();

    }
}
