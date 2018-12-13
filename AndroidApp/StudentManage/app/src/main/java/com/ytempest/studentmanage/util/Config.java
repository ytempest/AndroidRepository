package com.ytempest.studentmanage.util;

import android.os.Environment;

import java.io.File;

/**
 * @author ytempest
 *         Description：
 */
public class Config {
    /**
     * 程序外部存储的文件夹位置
     */
    public static final String EXTERNAL_DIR =
            Environment.getExternalStorageDirectory() + File.separator + "StudentManage";

    /**
     * 存储头像相关的文件夹
     */
    public static final String HEAD_DIR = EXTERNAL_DIR + File.separator + "head";

    /**
     * 添加学生和教师时默认的头像
     */
    public static final String HEAD_DEFAULT = HEAD_DIR + File.separator + "default.png";

    /**
     * 头像缓存
     */
    public static final String HEAD_TEM = HEAD_DIR + File.separator + "tem.png";

    /**
     * 上传到服务器的用户头像地址
     */
    public static final String HEAD_IMAGE = HEAD_DIR + File.separator + "head.png";

    /**
     * RecyclerView默认加载的数量
     */
    public static final int PAGE_SIZE = 10;

    /**
     * 下拉加载的时间，加载的时间低于阀值，则将加载的时间延长，以提高用户体验
     */
    public static final long MIN_LOAD_TIME = 1500;
}
