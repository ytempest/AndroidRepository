package com.ytempest.bsdiffdemo.util;

/**
 * Email 240336124@qq.com
 * Created by Darren on 2017/5/6.
 * Version 1.0
 * Description:
 */
public class PatchUtils {
    static{
        System.loadLibrary("bspatch");
    }
    /**
     * @param oldApkPath  原来的apk  1.0 本地安装的apk
     * @param newApkPath  合并后新的apk路径   需要生成的2.0
     * @param patchPath  差分包路径， 从服务器上下载下来
     */
    public static native void combine(String oldApkPath,String newApkPath,String patchPath);
}
