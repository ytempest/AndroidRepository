package com.ytempest.andfixdemo.andfix;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.alipay.euler.andfix.annotation.MethodReplace;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * @author ytempest
 *         Description：负责调用native方法对错误的Method进行替换
 */
public class AndFixManager {
    private static final String TAG = "AndFixManager";

    /**
     * 这个是存储dex解压后的相关文件
     */
    private final String optimizeDir;
    private final Context mContext;

    public AndFixManager(Context context) {
        this.mContext = context;
        this.optimizeDir = context.getFilesDir().getAbsolutePath();
        // 初始化底层修复逻辑
        HandlerNative.init(Build.VERSION.SDK_INT);
    }

    public void fix(File patchFile, ClassLoader parentClassLoader, List<String> clazzList) {
        File optFile = new File(optimizeDir, patchFile.getName());
        if (optFile.exists()) {
            optFile.delete();
        }

        try {
            final DexFile dexFile = DexFile.loadDex(patchFile.getAbsolutePath(), optFile.getAbsolutePath(), Context.MODE_PRIVATE);

            ClassLoader classLoader = new ClassLoader() {
                @Override
                protected Class<?> findClass(String className) throws ClassNotFoundException {
                    Class clazz = dexFile.loadClass(className, this);
                    if (clazz == null) {
                        clazz = Class.forName(className);
                    }
                    return clazz;
                }
            };

            // 获取dexFile中的所有的Class，如：com.ytempest.andfixdemo.Calculator_CF
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                // 如果这个clazz是要修复的Class
                String clazz = entries.nextElement();
                if (clazzList.contains(clazz)) {
                    // 将修复了bug的Class从DexFile中加载到内存中
                    // Class realClazz = classLoader.loadClass(key);
                    Class realClazz = dexFile.loadClass(clazz, classLoader);

                    // 如果这个Class加载成功，那么就开始修复这个Class要进行修复的bug
                    if (realClazz != null) {
                        fixClass(realClazz, classLoader);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 使用 realClazz修复相应的bug
     *
     * @param realClazz   修复了bug的Class
     * @param classLoader 类加载器
     */
    private void fixClass(Class realClazz, ClassLoader classLoader) {
        Method[] methods = realClazz.getDeclaredMethods();
        // 从这个Class中找到标记了MethodReplace注解的方法，然后在dalvik层替换出现bug的方法
        for (Method method : methods) {
            MethodReplace annotation = method.getAnnotation(MethodReplace.class);
            if (annotation != null) {
                // 获取出现bug的类名，如：com.ytempest.andfixdemo.Calculator
                String clazzName = annotation.clazz();
                // 获取出现bug的方法名，如：calculate
                String methodName = annotation.method();

                Log.i(TAG, "fixClass: 找到替换方法[ 类名：" + clazzName + "、 方法名：" + methodName + " ]");

                // method：修复了bug的方法
                replaceMethod(classLoader, clazzName, methodName, realClazz, method);
            }
        }
    }

    /**
     * 在这个方法中调用native方法将错误的Method方法替换成修复了Bug的Method方法
     *
     * @param classLoader  用于加载DexFile中的Class的类加载器
     * @param clazzName    出现Bug的Method所在的Class类名
     * @param methodName   出现Bug的Method名称
     * @param realClazz    修复了Bug的Class类
     * @param targetMethod 修复了Bug的Method方法
     */
    private void replaceMethod(ClassLoader classLoader, String clazzName, String methodName, Class realClazz, Method targetMethod) {
        try {
            Class srcClazz = Class.forName(clazzName);
            if (srcClazz != null) {
                // 获取出现bug的Method方法
                Method srcMethod = srcClazz.getDeclaredMethod(methodName, targetMethod.getParameterTypes());

                // 在dalvik层替换方法
                HandlerNative.replaceMethod(srcMethod, targetMethod);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
