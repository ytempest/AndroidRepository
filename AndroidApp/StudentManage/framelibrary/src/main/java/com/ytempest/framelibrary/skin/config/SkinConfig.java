package com.ytempest.framelibrary.skin.config;

/**
 * @author ytempest
 *         Description: 皮肤资源的配置类，用于标识换肤路径，以及表示获取皮肤出现的问题
 */
public class SkinConfig {
    /**
     * 皮肤的 SP的文件名称
     */
    public static final String SKIN_INFO_NAME = "skinInfo";
    /**
     * 标识皮肤的路径
     */
    public static final String SKIN_PATH_NAME = "skinPath";
    /**
     * 表示换肤成功
     */
    public static final int SKIN_LOADED_SUCCESS = 1;
    /**
     * 表示当前app中没有该皮肤
     */
    public static final int SKIN_NOTEXIST = -1;
    /**
     * 表示资源包中的皮肤文件不存在
     */
    public static final int SKIN_FILE_NOTEXIST = -2;
    /**
     * 表示皮肤文件不是一个apk
     */
    public static final int SKIN_FILE_ERROR = -3;
}
