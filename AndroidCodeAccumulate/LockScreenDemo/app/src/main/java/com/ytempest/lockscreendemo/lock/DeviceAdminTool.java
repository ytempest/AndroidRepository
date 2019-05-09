package com.ytempest.lockscreendemo.lock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * @author ytempest
 * @date 2019/4/28
 */
public class DeviceAdminTool {

    private static final String TAG = DeviceAdminTool.class.getSimpleName();

    private static DeviceAdminTool INSTANCE;
    private DevicePolicyManager mDeviceManager;
    private ComponentName mComponentName;
    private Context mContext;

    public static synchronized DeviceAdminTool getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DeviceAdminTool();
        }
        return INSTANCE;
    }

    /**
     * 初始化“设备管理权限的获取”
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();
        // 获取系统管理权限
        mDeviceManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // 申请权限
        mComponentName = new ComponentName(mContext, MyAdminReceiver.class);

        setDeviceAdminActive(true);
    }

    /**
     * 判断App是否已激活获取了设备管理权限,true:已激活，false:未激活
     * app预装，启动后要授权静默激活
     */
    private boolean isAdminActive() {
        // 判断组件是否有系统管理员权限
        return mDeviceManager.isAdminActive(mComponentName);
    }

    @Deprecated
    private void setDeviceAdminActive(boolean active) {
        try {
            if (mDeviceManager != null && mComponentName != null) {
                Log.e(TAG, "setDeviceAdminActive: 篡改组件权限");
                Method setActiveAdmin = mDeviceManager.getClass().getDeclaredMethod("setActiveAdmin", ComponentName.class, boolean.class);
                setActiveAdmin.setAccessible(true);
                setActiveAdmin.invoke(mDeviceManager, mComponentName, active);
                Log.e(TAG, "setDeviceAdminActive: 篡改结果：" + isAdminActive());
            }
        } catch (Exception e) {
            Log.e(TAG, "setDeviceAdminActive: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* Lock */

    public void activateLock() {
        Intent intent = new Intent(
                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        // 附加数据
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                mComponentName);
        // 描述
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "激活后可以方便锁屏");
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(intent);
    }

    public void lockScreen() {
        Log.e(TAG, "lockScreen: 是否有管理员权限：" + (isAdminActive() && mDeviceManager != null));
        if (isAdminActive() && mDeviceManager != null) {
            // 立刻锁屏
            mDeviceManager.lockNow();
        }
    }

}