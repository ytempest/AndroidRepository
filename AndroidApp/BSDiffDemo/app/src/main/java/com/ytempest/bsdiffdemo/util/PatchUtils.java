package com.ytempest.bsdiffdemo.util;

import android.content.pm.Signature;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ytempest
 *         Description: 实现增量更新，合并差分包
 */
public class PatchUtils {
    static {
        System.loadLibrary("bspatch");
    }


    /**
     * 获取该目录下的apk签名
     *
     * @param path 当前apk路径
     */
    public static String getSignature(String path) throws ReflectiveOperationException {
        // 1.反射实例化PackageParser对象
        Object packageParser = getPackageParser(path);

        // 2.反射获取parsePackage方法
        Object packageObject = getPackageInfo(path, packageParser);

        // 3.调用collectCertificates方法
        Method collectCertificatesMethod = packageParser.getClass()
                .getDeclaredMethod("collectCertificates", packageObject.getClass(), int.class);

        collectCertificatesMethod.invoke(packageParser, packageObject, 0);

        // 4.获取mSignatures属性
        Field signaturesField = packageObject.getClass().getDeclaredField("mSignatures");
        signaturesField.setAccessible(true);
        Signature[] mSignatures = (Signature[]) signaturesField.get(packageObject);
        return mSignatures[0].toCharsString();
    }


    /**
     * 创建PackageParser.Package类，兼容5.0
     */
    private static Object getPackageInfo(String path, Object packageParser) throws ReflectiveOperationException{
        Class<?>[] paramClass;
        Object[] paramObject;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            paramClass = new Class[2];
            paramClass[0] = File.class;
            paramClass[1] = int.class;

            paramObject = new Object[2];
            paramObject[0] = new File(path);
            paramObject[1] = 0;
        } else {
            paramClass = new Class[4];
            paramClass[0] = File.class;
            paramClass[1] = String.class;
            paramClass[2] = DisplayMetrics.class;
            paramClass[3] = int.class;

            paramObject = new Object[4];
            paramObject[0] = new File(path);
            paramObject[1] = path;
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            paramObject[2] = metrics;
            paramObject[3] = 0;
        }

        Method parsePackageMethod = packageParser.getClass().getDeclaredMethod("parsePackage", paramClass);
        parsePackageMethod.setAccessible(true);
        return parsePackageMethod.invoke(packageParser, paramObject);
    }


    /**
     * 创建PackageParser类
     */
    private static Object getPackageParser(String path) throws ReflectiveOperationException {
        Class<?> packageParserClazz = Class.forName("android.content.pm.PackageParser");
        // 兼容5.0版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Constructor<?> packageParserConstructor = packageParserClazz.getDeclaredConstructor();
            return packageParserConstructor.newInstance();
        } else {
            Constructor<?> packageParserConstructor = packageParserClazz.getDeclaredConstructor(String.class);
            return packageParserConstructor.newInstance(path);
        }
    }


    /**
     * @param oldApkPath 原来的apk，本地安装的apk
     * @param newApkPath 新的apk路径
     * @param patchPath  差分包路径，从服务器上下载下来
     */
    public static native void combine(String oldApkPath, String newApkPath, String patchPath);
}
