package com.ytempest.test2;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.alipay.euler.andfix.patch.PatchManager;
import com.ytempest.baselibrary.exception.ExceptionCrashHandler;
import com.ytempest.baselibrary.fixbug.FixDexManager;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.framelibrary.http.OkHttpEngine;
import com.ytempest.framelibrary.skin.SkinManager;

/**
 * create by ytempest at 2017/10/24/024
 * Description:
 */
public class BaseApplication extends Application {

    public static PatchManager mPatchManager;
    public static FixDexManager mFixDexManager;

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置全部异常捕捉类
        ExceptionCrashHandler.getInstance().init(this);
        // 初始化皮肤管理器
        SkinManager.getInstance().init(this);

        HttpUtils.init(new OkHttpEngine());

        /*mPatchManager = new PatchManager(this);

        String appVersion = getVersionName();

        mPatchManager.init(appVersion);
        mPatchManager.loadPatch();*/

        try {
            mFixDexManager = new FixDexManager(this);
            mFixDexManager.loadFixDex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getVersionName() {
            String verCode = null;
        try{
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            verCode = packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return verCode;
    }
}
