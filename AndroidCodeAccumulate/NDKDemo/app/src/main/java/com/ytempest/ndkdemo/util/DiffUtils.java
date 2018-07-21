package com.ytempest.ndkdemo.util;

/**
 * @author ytempest
 *         Description：
 */
public class DiffUtils {

    static {
        System.loadLibrary("file_diff");
    }

    /**
     * 拆分文件
     *
     * @param filePath    要进行拆分的文件
     * @param filePattern 拆分后的文件格式以及路径
     * @param patchCount  要拆分的数量
     */
    public static native void diff(String filePath, String filePattern, int patchCount);

    /**
     * 合并文件
     *
     * @param mergePath   合并后的文件位置
     * @param filePattern 要进行合并的文件格式以及路径
     * @param patchCount  要合并的数量
     */
    public static native void merge(String mergePath, String filePattern, int patchCount);
}
