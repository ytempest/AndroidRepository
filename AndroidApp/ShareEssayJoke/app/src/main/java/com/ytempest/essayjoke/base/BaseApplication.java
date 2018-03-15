package com.ytempest.essayjoke.base;

import android.app.Application;

import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.ytempest.baselibrary.exception.ExceptionCrashHandler;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.framelibrary.http.OkHttpEngine;
import com.ytempest.framelibrary.skin.SkinManager;


/**
 * @author ytempest
 *         Description:
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化网络引擎
        HttpUtils.init(new OkHttpEngine());

        // 初始化换肤框架
        SkinManager.getInstance().init(this);

        // 设置全局异常捕捉类
        ExceptionCrashHandler.getInstance().init(this);


        UMShareAPI.get(this);
        PlatformConfig.setWeixin("wx967daebe835fbeac", "5bb696d9ccd75a38c8a0bfe0675559b3");
        PlatformConfig.setQQZone("100424468", "c7394704798a158208a74ab60104f0ba");
        PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad", "http://sns.whalecloud.com");


        // 初始化阿里的热修复
        /*mPatchManager = new PatchManager(this);

        try {
            // 初始化版本，获取当前应用的版本
            PackageManager packageManager = this.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            mPatchManager.init(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // 加载之前的 apatch 包
        mPatchManager.loadPatch();*/

/*        try {
            // 很耗时  热启动和冷启动  2s   400 ms
            FixDexManager fixDexManager = new FixDexManager(this);
            // 加载所有修复的Dex包
            fixDexManager.loadFixDex();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
