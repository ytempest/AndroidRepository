package com.ytempest.baselibrary.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * @author ytempest
 *         Description：一个Android6.0权限申请框架
 */
public class SmartPermission {

    private Object mObject;
    /**
     * 权限请求码
     */
    private int mRequestCode;
    /**
     * 申请的权限
     */
    private String[] mRequestPermission;

    private SmartPermission(Object object) {
        this.mObject = object;
    }

    public static SmartPermission with(@NonNull Activity activity) {
        return new SmartPermission(activity);
    }

    public static SmartPermission with(@NonNull Fragment fragment) {
        return new SmartPermission(fragment);
    }

    public SmartPermission requestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    public SmartPermission requestPermission(@NonNull String... permissions) {
        this.mRequestPermission = permissions;
        return this;
    }

    /**
     * 开始申请权限
     */
    public void request() {
        // 如果系统不是6.0或以上的，直接运行成功的方法
        if (!PermissionUtils.isOverMarshmallow()) {
            PermissionUtils.executeSucceedMethod(mObject, mRequestCode);
            return;
        }

        if (mObject == null) {
            throw new NullPointerException("Please use the with() method initialize the SmartPermission! ");
        }

        // 获取要申请的权限没有授权的权限列表
        List<String> deniedPermissions = PermissionUtils.getDeniedPermissions(mObject, mRequestPermission);

        // 如果当前申请的权限都已经授权了就直接运行成功的方法，否则申请没有授权的权限
        if (deniedPermissions.size() == 0) {
            PermissionUtils.executeSucceedMethod(mObject, mRequestCode);
        } else {
            // 由于Activity和Fragment申请权限回调的方法不一样，所以要区分申请权限的方法
            if (mObject instanceof Fragment) {
                ((Fragment) mObject).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]),
                        mRequestCode);
                return;
            }
            ActivityCompat.requestPermissions((Activity) mObject,
                    deniedPermissions.toArray(new String[deniedPermissions.size()]), mRequestCode);
        }
    }

    /**
     * 申请权限结果回调方法
     *
     * @param object       Fragment对象 或 Activity对象
     * @param grantResults 申请结果
     */
    public static void onRequestPermissionResult(@NonNull Object object, int requestCode, int[] grantResults) {
        if (grantResults != null && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.executeSucceedMethod(object, requestCode);
            } else {
                PermissionUtils.executeFailMethod(object, requestCode);
            }
        }
    }
}
