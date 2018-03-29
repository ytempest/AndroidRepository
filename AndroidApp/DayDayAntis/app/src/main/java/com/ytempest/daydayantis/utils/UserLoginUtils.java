package com.ytempest.daydayantis.utils;

import android.content.Context;

/**
 * @author ytempest
 *         Description：
 */
public class UserLoginUtils {

    public static boolean isUserLogin(Context context) {
        return SpUtils.getInstance(context)
                .getBoolean(SpConfig.USER_INFO, SpConfig.USER_IS_LOGIN, false);
    }

    public static void saveUserLoginStatus(Context context, boolean status) {
        SpUtils.getInstance(context)
                .putBoolean(SpConfig.USER_INFO, SpConfig.USER_IS_LOGIN, status);
    }

    public static void setUserInfo(Context context, String userInfo) {
        SpUtils.getInstance(context)
                .putString(SpConfig.USER_INFO, SpConfig.USER_DATE_RESULT, userInfo);
    }

    public static String saveUserInfo(Context context) {
        String userInfoString = SpUtils.getInstance(context)
                .getString(SpConfig.USER_INFO, SpConfig.USER_DATE_RESULT, "");
        return userInfoString;
    }

}
