package com.ytempest.studentmanage.util;

import android.content.Context;

/**
 * @author ytempest
 *         Description：用户登录的工具类，用来判断当前的用户类型，以及提供用户信息的存储和获取
 */
public class LoginInfoUtils {
    /**
     * 用户登录的信息文件名
     */
    private static String LOGIN_INFO = "login_info";
    /**
     * 用户类型
     */
    private static String USER_TYPE = "user_type";
    public static String USER_TYPE_STUDENT = "student";
    public static String USER_TYPE_TEACHER = "teacher";
    public static String USER_TYPE_MANAGER = "manager";

    /**
     * 用户账号
     */
    private static String USER_ACCOUNT = "user_account";

    /**
     * 标识用户是否登录
     */
    private static String IS_USER_LOGIN = "is_user_login";

    private LoginInfoUtils() {
    }

    /**
     * 判断用户是否已经登录，已经登录则返回true
     */
    public static boolean isUserLogined(Context context) {
        return SpUtils.getInstance(context).getBoolean(LOGIN_INFO, IS_USER_LOGIN, false);
    }

    /**
     * 获取用户类型
     *
     * @return USER_TYPE_STUDENT、USER_TYPE_TEACHER、USER_TYPE_MANAGER
     */
    public static String getUserType(Context context) {
        return SpUtils.getInstance(context).getString(LOGIN_INFO, USER_TYPE, "");
    }

    /**
     * 获取用户账号
     */
    public static String getUserAccount(Context context) {
        return SpUtils.getInstance(context).getString(LOGIN_INFO, USER_ACCOUNT, "");
    }

    public static boolean isStudent(Context context) {
        return getUserType(context).equals(USER_TYPE_STUDENT);
    }

    public static boolean isTeacher(Context context) {
        return getUserType(context).equals(USER_TYPE_TEACHER);
    }

    public static boolean isManager(Context context) {
        return getUserType(context).equals(USER_TYPE_MANAGER);
    }

    /**
     * 存储用户的登录信息
     *
     * @param userType    用户类型
     * @param userAccount 用户账号
     */
    public static void saveUserLoginInfo(Context context, String userType, String userAccount) {
        SpUtils instance = SpUtils.getInstance(context);
        // 设置用户已经登录
        instance.putBoolean(LOGIN_INFO, IS_USER_LOGIN, true);
        // 保存用户类型和用户账号
        instance.putString(LOGIN_INFO, USER_TYPE, userType);
        instance.putString(LOGIN_INFO, USER_ACCOUNT, userAccount);
    }

    /**
     * 清除用户登录信息
     */
    public static void clearUserLoginData(Context context) {
        SpUtils instance = SpUtils.getInstance(context);
        // 注销用户
        instance.putBoolean(LOGIN_INFO, IS_USER_LOGIN, false);
        // 保存用户类型和用户账号
        instance.putString(LOGIN_INFO, USER_TYPE, "");
        instance.putString(LOGIN_INFO, USER_ACCOUNT, "");
    }

}
