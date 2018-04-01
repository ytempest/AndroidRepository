package com.ytempest.daydayantis.utils;

import android.content.Context;

/**
 * @author ytempest
 *         Description：这是一个连接 用户信息存储实现的一个工具类，这个
 *         工具类是提供给开发人员使用，这样可以对用户信息的存储实现隐藏
 */
public class UserInfoUtils {

    /**
     * 获取用户的登录状态
     *
     * @return true为已经登录，否则没有
     */
    public static boolean isUserLogin(Context context) {
        return SpUtils.getInstance(context)
                .getBoolean(SpConfig.USER_INFO, SpConfig.USER_IS_LOGIN, false);
    }

    /**
     * 存储用户登录的状态
     */
    public static void saveUserLoginStatus(Context context, boolean status) {
        SpUtils.getInstance(context)
                .putBoolean(SpConfig.USER_INFO, SpConfig.USER_IS_LOGIN, status);
    }

    /**
     * 存储用户的信息
     */
    public static void saveUserInfo(Context context, String userInfo) {
        SpUtils.getInstance(context)
                .putString(SpConfig.USER_INFO, SpConfig.USER_DATE_RESULT, userInfo);
    }

    /**
     * 获取用户信息
     */
    public static String getUserInfo(Context context) {
        String userInfoString = SpUtils.getInstance(context)
                .getString(SpConfig.USER_INFO, SpConfig.USER_DATE_RESULT, "");
        return userInfoString;
    }

}
