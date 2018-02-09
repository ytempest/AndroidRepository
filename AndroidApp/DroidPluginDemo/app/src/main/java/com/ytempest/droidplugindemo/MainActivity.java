package com.ytempest.droidplugindemo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.morgoo.droidplugin.pm.PluginManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
            + "PluginApp.apk";
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void install(View view) {
        try {
            // flags可以设置为0，如果要更新插件，则设置为PackageManagerCompat.INSTALL_REPLACE_EXISTING
            int result = PluginManager.getInstance().installPackage(apkPath, 0);
            Log.e(TAG, "install: result --> " + result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void unInstall(View view) {
        try {
            PluginManager.getInstance().deletePackage(apkPath, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void start(View view) {
        // 启动插件，启动方法格式只能是这样
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        String packageName = info.packageName;
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        intent.putExtra("user_name", "ytempest");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
