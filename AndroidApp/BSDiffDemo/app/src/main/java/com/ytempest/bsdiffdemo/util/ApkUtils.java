package com.ytempest.bsdiffdemo.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * @author ytempest
 *         Description：
 */
public class ApkUtils {

    /**
     * 判断指定包名的apk是否已经安装
     */
    public static boolean isInstall(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        boolean isInstall = false;
        try {
            packageManager.getPackageArchiveInfo(packageName, PackageManager.GET_ACTIVITIES);
            isInstall = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isInstall;
    }


    /**
     * 获取指定包名的 apk的源文件路径，这个路径一般在 /data/data下面
     */
    public static String getSourceApkPath(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            return applicationInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 安装apk
     */
    public static void installApk(Context context, String apkPath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkPath),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
