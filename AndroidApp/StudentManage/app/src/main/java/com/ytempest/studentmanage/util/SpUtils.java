package com.ytempest.studentmanage.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author ytempest
 *         Description：
 */
public class SpUtils {
    private static volatile SpUtils INSTANCE;
    private Context mContext;

    private SpUtils(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static SpUtils getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SpUtils.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SpUtils(context);

                }
            }
        }
        return INSTANCE;
    }

    public void putBoolean(String name, String key, boolean value) {
        SharedPreferences sp = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        sp.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String name, String key, boolean defaultValue) {
        SharedPreferences sp = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    public void putString(String name, String key, String value) {
        SharedPreferences sp = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public String getString(String name, String key, String defaultValue) {
        SharedPreferences sp = mContext.getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }
}
