package com.ytempest.lockscreendemo.lock;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

/**
 * @author ytempest
 * @date 2019/4/28
 */
public class MyAdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "CustomAdminReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        context.stopService(intent);// 是否可以停止
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.e(TAG, "onEnabled: 设备管理：可用");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.e(TAG, "onDisabled: 设备管理：不可用");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.e(TAG, "onDisableRequested: 设备管理：不可用");
        // 这里处理 不可编辑设备。
        context.stopService(intent);// 是否可以停止
        return "禁止用户的请求"; // "这是一个可选的消息，警告有关禁止用户的请求";
    }
}
