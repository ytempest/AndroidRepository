package com.ytempest.baselibrary.fixbug;

import android.content.Context;
import android.util.Log;

import com.ytempest.baselibrary.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.BaseDexClassLoader;

/**
 * @author ytempest
 *         Description: Dex热修复
 */
public class FixDexManager {
    private static final String TAG = "FixDexManager";
    private Context mContext;
    /**
     * 存放dex包的文件夹
     */
    private File mDexDir;

    public FixDexManager(Context context) {
        this.mContext = context;
        // 获取应用可以访问的dex目录
        this.mDexDir = context.getDir("dexdir", Context.MODE_PRIVATE);
    }


    /**
     * 对外接口方法，修复dex包
     *
     * @param fixDexPath
     */
    public void fixDex(String fixDexPath) throws Exception {
        // 获取下载好的补丁的 dexElement
        // 移动到系统能够访问的 dexdir 目录下
        File srcFile = new File(fixDexPath);

        if (!srcFile.exists() || !srcFile.getName().endsWith(".dex")) {
            throw new FileNotFoundException(fixDexPath);
        }
        // 创建存放在 dexdir 目录下的dex文件路径
        File destFile = new File(mDexDir, srcFile.getName());

        // 如果该dex文件已经存在，则return
        if (destFile.exists()) {
            Log.d(TAG, "patch [" + fixDexPath + "] has be loaded.");
            return;
        }

        // 将srcFile 复制到 destFile
        FileUtil.copyFile(srcFile, destFile);

        // ClassLoader读取fixDex路径
        // 为什么加入到集合，因为程序一启动 loadFixDex方法就要修复bug
        List<File> fixDexFileList = new ArrayList<>();
        fixDexFileList.add(destFile);

        fixDexFiles(fixDexFileList);
    }

    /**
     * 把dexElements注入到classLoader中
     *
     * @param classLoader
     * @param dexElements
     */
    private void injectDexElements(ClassLoader classLoader, Object dexElements) throws Exception {
        // 1. 先获取 pathList
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);

        // 2. 获取 pathList里面的dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);

        dexElementsField.set(pathList, dexElements);
    }


    /**
     * 通过反射合并两个数组，这样就可以忽略数组的类型就行合并
     *
     * @param arrayLhs
     * @param arrayRhs
     * @return 合并好的数组
     */
    private static Object combineArray(Object arrayLhs, Object arrayRhs) {
        Class<?> localClass = arrayLhs.getClass().getComponentType();
        int i = Array.getLength(arrayLhs);
        int j = i + Array.getLength(arrayRhs);
        Object result = Array.newInstance(localClass, j);
        for (int k = 0; k < j; ++k) {
            if (k < i) {
                Array.set(result, k, Array.get(arrayLhs, k));
            } else {
                Array.set(result, k, Array.get(arrayRhs, k - i));
            }
        }
        return result;
    }

    /**
     * 从指定的classLoader类加载器中中获取 该类加载器中相应的dexElements
     *
     * @param classLoader
     * @return dexElements
     */
    private Object getDexElementsByClassLoader(ClassLoader classLoader) throws Exception {

        // 1. 先获取 pathList
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(classLoader);

        // 2. 获取 pathList里面的dexElements
        Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);

        return dexElementsField.get(pathList);
    }

    /**
     * 设置在程序启动后自动调用，然后加载全部的修复包并进行修复
     */
    public void loadFixDex() throws Exception {
        File[] dexFiles = mDexDir.listFiles();

        List<File> fixDexFileList = new ArrayList<>();

        for (File dexFile : dexFiles) {
            if (dexFile.getName().endsWith(".dex")) {
                fixDexFileList.add(dexFile);
            }
        }

        // 进行热修复
        fixDexFiles(fixDexFileList);
    }

    /**
     * 通过将每一个修复了bug的dex文件插到没有修复bug的dex文件前面来进行热修复
     *
     * @param fixDexFileList
     */
    private void fixDexFiles(List<File> fixDexFileList) throws Exception {
        // 1. 先获取已经运行的含有 bug的 dexElement
        ClassLoader applicationClassLoader = mContext.getClassLoader();

        Object applicationDexElements = getDexElementsByClassLoader(applicationClassLoader);

        // optimizedDir  解压路径
        File optimizedDir = new File(mDexDir, "odex");

        if (!optimizedDir.exists()) {
            optimizedDir.mkdirs();
        }

        // 遍历获取每一个 dex文件的 dexElement
        for (File fixDexFile : fixDexFileList) {
            /**
             * 作用：获取dex文件的类加载器
             * dexPath  dex路径
             * optimizedDir  解压路径
             * libraryPath .so文件位置
             * parent 父ClassLoader
             */
            ClassLoader fixDexClassLoader = new BaseDexClassLoader(
                    // dex路径  必须要在应用目录下的odex文件中
                    fixDexFile.getAbsolutePath(),
                    // 解压路径
                    optimizedDir,
                    // .so文件位置
                    null,
                    // 父ClassLoader
                    applicationClassLoader
            );

            // 获取下载的解决了 bug的 dexElement
            Object fixDexElements = getDexElementsByClassLoader(fixDexClassLoader);

            // 3.1 把补丁的dexElement 插到 已经运行的 dexElement 的最前面，合并applicationDexElements
            // 数组 合并 fixDexElements 数组，合并好的数组放在applicationDexElements
            applicationDexElements = combineArray(fixDexElements, applicationDexElements);
        }

        // 3.2 把合并的数组注入到原来的 applicationClassLoader类加载器中，这样
        // 程序运行的时候就会自动加载 修复了bug的dex文件而不会加载有bug的文件
        injectDexElements(applicationClassLoader, applicationDexElements);
    }

}
