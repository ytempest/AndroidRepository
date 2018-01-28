package com.ytempest.framelibrary.skin.config;

/**
 * @author ytempest
 *         Description: SkinPreUtils类的配置类
 */
public class SkinConfig {
    /**
     * 皮肤的 SP的文件名称
     */
    public static final String SKIN_INFO_NAME = "skinInfo";
    /**
     * 皮肤的路径
     */
    public static final String SKIN_PATH_NAME = "skinPath";
    /**
     * 表示没有皮肤
     */
    public static final int SKIN_LOADED = -1;
    /**
     * 表示换肤成功
     */
    public static final int SKIN_LOADED_SUCCESS = 1;
    /**
     * 皮肤文件不存在
     */
    public static final int SKIN_FILE_NOEXIST = -2;
    /**
     * 获取皮肤文件包名失败，皮肤文件不是一个apk
     */
    public static final int SKIN_FILE_ERROR = -3;
}
