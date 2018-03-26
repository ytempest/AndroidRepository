package com.ytempest.framelibrary.view.binnerview;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * @author ytempest
 *         Description：实现了 Application.ActivityLifecycleCallbacks的所有方法，子类
 *         继承这个类选择需要复写的方法，避免子类直接实现造成代码量过大
 */
public class DefaultActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
