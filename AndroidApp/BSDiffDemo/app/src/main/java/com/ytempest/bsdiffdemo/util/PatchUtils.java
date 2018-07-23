package com.ytempest.bsdiffdemo.util;

import android.content.pm.Signature;
import android.os.Build;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
     * 获取指定路径下的apk签名
     *
     * @param apkPath apk路径
     */
    public static String getSignature(String apkPath) throws ReflectiveOperationException {
        // 1.反射实例化PackageParser对象
        Object packageParserObject = getPackageParserObject(apkPath);

        // 2.解析path路径的apk的包信息
        Object packageObject = getPackageObject(apkPath, packageParserObject);

        // 3.调用collectCertificates方法
        Method collectCertificatesMethod = packageParserObject.getClass()
                .getDeclaredMethod("collectCertificates", packageObject.getClass(), int.class);

        // 4.反射执行 collectCertificates方法后，得到的apk签名保存在 mSignatures
        collectCertificatesMethod.invoke(packageParserObject, packageObject, 0);

        // 5.获取mSignatures属性
        Field signaturesField = packageObject.getClass().getDeclaredField("mSignatures");
        signaturesField.setAccessible(true);
        Signature[] mSignatures = (Signature[]) signaturesField.get(packageObject);
        return mSignatures[0].toCharsString();
    }


    /**
     * 通过反射执行 parsePackage方法解析apkPath路径的apk的包信息，保存到一个Package对象并返回
     * 兼容所有版本
     */
    private static Object getPackageObject(String apkPath, Object packageParser) throws ReflectiveOperationException {
        Class<?>[] paramClass;
        Object[] paramObject;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 如果是 5.0版本及以上
            paramClass = new Class[2];
            paramClass[0] = File.class;
            paramClass[1] = int.class;

            paramObject = new Object[2];
            paramObject[0] = new File(apkPath);
            paramObject[1] = 0;
        } else {
            paramClass = new Class[4];
            paramClass[0] = File.class;
            paramClass[1] = String.class;
            paramClass[2] = DisplayMetrics.class;
            paramClass[3] = int.class;

            paramObject = new Object[4];
            paramObject[0] = new File(apkPath);
            paramObject[1] = apkPath;
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            paramObject[2] = metrics;
            paramObject[3] = 0;
        }

        Method parsePackageMethod = packageParser.getClass().getDeclaredMethod("parsePackage", paramClass);
        parsePackageMethod.setAccessible(true);
        // 反射执行 parsePackage方法
        return parsePackageMethod.invoke(packageParser, paramObject);
    }


    /**
     * 通过放射创建解析Apk的PackageParser类
     */
    private static Object getPackageParserObject(String apkPath) throws ReflectiveOperationException {
        Class<?> packageParserClazz = Class.forName("android.content.pm.PackageParser");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 如果是 5.0版本及以上
            Constructor<?> packageParserConstructor = packageParserClazz.getDeclaredConstructor();
            return packageParserConstructor.newInstance();
        } else {
            Constructor<?> packageParserConstructor = packageParserClazz.getDeclaredConstructor(String.class);
            return packageParserConstructor.newInstance(apkPath);
        }
    }


    /**
     * @param oldApkPath 原来的apk，本地安装的apk
     * @param newApkPath 新的apk路径
     * @param patchPath  差分包路径，从服务器上下载下来
     */
    public static native void patch(String oldApkPath, String newApkPath, String patchPath);
}
