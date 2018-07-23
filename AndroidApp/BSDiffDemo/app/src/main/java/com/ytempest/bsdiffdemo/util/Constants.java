package com.ytempest.bsdiffdemo.util;

import android.os.Environment;

import java.io.File;

/**
 * @author ytempest
 *         Description：
 */
public class Constants {
    // 差分包的名称
    public static final String PATCH_FILE = "apk.patch";

    // 差分包在服务器上的路径
    public static final String URL_PATCH_DOWNLOAD = "http://172.18.41.91/ApkBsdiffService/" + PATCH_FILE;

    // 当前apk的包名
    public static final String PACKAGE_NAME = "com.ytempest.bsdiffdemo";

    // 下载后的文件的保存位置
    public static final String CARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "aaa" + File.separator;

    // 合并好的新apk的保存位置
    public static final String NEW_APK_PATH = CARD_PATH + "new.apk";

    // 下载的差分包的保存位置
    public static final String PATCH_FILE_PATH = CARD_PATH + PATCH_FILE;
}
