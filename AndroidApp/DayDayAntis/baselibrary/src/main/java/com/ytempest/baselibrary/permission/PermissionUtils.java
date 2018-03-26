package com.ytempest.baselibrary.permission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：
 */
public class PermissionUtils {

    /**
     * 防止创建对象
     */
    private PermissionUtils() {
        throw new UnsupportedOperationException("PermissionUtils cannot be instantiated !");
    }

    /**
     * 判断是否为6.0及以上的版本
     */
    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 执行申请权限成功的方法
     */
    public static void executeSucceedMethod(Object reflectObject, int requestCode) {
        Method[] methods = reflectObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            // 获取做了标志的方法，并执行该方法
            PermissionSucceed annotation = method.getAnnotation(PermissionSucceed.class);
            if (annotation != null) {
                int markRequestCode = annotation.requestCode();
                if (requestCode == markRequestCode) {
                    executeMethod(reflectObject, method);
                }
            }
        }
    }

    /**
     * 执行申请权限失败的方法
     */
    public static void executeFailMethod(Object reflectObject, int requestCode) {

        Method[] methods = reflectObject.getClass().getDeclaredMethods();
        for (Method method : methods) {
            PermissionFail annotation = method.getAnnotation(PermissionFail.class);
            if (annotation != null) {
                int markRequestCode = annotation.requestCode();
                if (requestCode == markRequestCode) {
                    executeMethod(reflectObject, method);
                }
            }
        }

    }

    private static void executeMethod(Object reflectObject, Method method) {
        // 如果该方法不是一个无参方法就抱异常
        if (method.getParameterTypes().length > 0) {
            throw new RuntimeException(
                    "Cannot execute method " + method.getName() + "() because this method request without arguments");
        }
        try {
            method.setAccessible(true);
            method.invoke(reflectObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取requestPermission中还没有授权的权限
     *
     * @param object             Fragment对象 或 Activity对象
     * @param requestPermissions 需要申请的权限
     * @return 一个还没有申请的权限的集合
     */
    public static List<String> getDeniedPermissions(Object object, String[] requestPermissions) {

        List<String> deniedPermissions = new ArrayList<>();

        for (String permission : requestPermissions) {
            int result = ActivityCompat.checkSelfPermission(getActivity(object), permission);
            // 如果该权限还没有授权就加入集合
            if (result == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    private static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return (Activity) object;
        }

        if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        }

        return null;
    }
}
