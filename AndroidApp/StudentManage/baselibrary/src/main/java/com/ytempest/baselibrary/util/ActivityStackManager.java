package com.ytempest.baselibrary.util;

import android.app.Activity;

import java.util.HashMap;

/**
 * @author ytempest
 *         Description：Activity的管理类，如同Activity的任务栈一样管理Activity，不过这个只负责
 *         管理Activity的 finish()
 */
public class ActivityStackManager {

    /**
     * Activity
     */
    private static HashMap<String, Activity> mActivities;
    private static ActivityStackManager mInstance;

    private ActivityStackManager() {
        mActivities = new HashMap<>();
    }

    public static ActivityStackManager getInstance() {
        if (mInstance == null) {
            synchronized (ActivityStackManager.class) {
                if (mInstance == null) {
                    mInstance = new ActivityStackManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 注册Activity
     */
    public void registerActivity(Activity activity) {
        mActivities.put(activity.getClass().getName(), activity);
    }

    /**
     * 通过调用 Activity的 finish() 方法关闭 Activity
     */
    public void finishActivity(Activity activity) {
        String key = activity.getClass().getName();
        Activity finishActivity = mActivities.get(key);
        if (finishActivity == null) {
            return;
        }
        finishActivity.finish();
        mActivities.remove(key);
    }

    /**
     * 通过调用 Activity的 finish() 方法关闭 Activity，同时会注销Activity
     */
    public void finishActivity(Class<? extends Activity> activityClazz) {
        String key = activityClazz.getName();
        Activity finishActivity = mActivities.get(key);
        if (finishActivity == null) {
            return;
        }
        finishActivity.finish();
        mActivities.remove(key);
    }

    /**
     * 注销 Activity，防止持有Activity的引用导致Activity无法被回收从而到导致内存泄漏
     */
    public void unregisterActivity(Activity activity) {
        String key = activity.getClass().getName();
        if (!mActivities.containsKey(key)) {
            return;
        }
        mActivities.remove(key);
    }

}
