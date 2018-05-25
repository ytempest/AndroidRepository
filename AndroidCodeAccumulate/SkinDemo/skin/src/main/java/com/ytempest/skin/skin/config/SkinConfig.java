package com.ytempest.skin.skin.config;

/**
 * @author ytempest
 *         Description: 皮肤资源的配置类，用于标识换肤路径，以及表示获取皮肤出现的问题
 */
public class SkinConfig {
    /**
     * 皮肤的 SP的文件名称
     */
    public static final String SKIN_INFO = "skin_info";
    /**
     * 标识皮肤的路径
     */
    public static final String SKIN_PATH = "skin_path";
    /**
     * 表示要加载的皮肤就是当前的皮肤
     */
    public static final int SKIN_HAD_LOADED = 1;

    /**
     * 表示资源包中的皮肤文件不存在
     */
    public static final int SKIN_NOT_EXIST = -1;
    /**
     * 表示皮肤文件不是一个apk
     */
    public static final int SKIN_FILE_ERROR = -2;
}
