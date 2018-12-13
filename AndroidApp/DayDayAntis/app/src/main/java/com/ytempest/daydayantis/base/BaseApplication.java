package com.ytempest.daydayantis.base;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.ytempest.baselibrary.exception.ExceptionCrashHandler;
import com.ytempest.baselibrary.http.HttpUtils;
import com.ytempest.baselibrary.imageloader.ImageLoaderManager;
import com.ytempest.framelibrary.http.OkHttpEngine;
import com.ytempest.framelibrary.imageloader.GlideImageLoader;
import com.ytempest.framelibrary.skin.SkinManager;


/**
 * @author ytempest
 *         Description：
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化百度地图
        SDKInitializer.initialize(getApplicationContext());

        // 初始化图片统一加载框架
        ImageLoaderManager.getInstance().init(new GlideImageLoader());

        // 初始化网络引擎
        HttpUtils.init(new OkHttpEngine());

        // 初始化换肤框架
        SkinManager.getInstance().init(this);

        // 设置全局异常捕捉类
        ExceptionCrashHandler.getInstance().init(this);

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
