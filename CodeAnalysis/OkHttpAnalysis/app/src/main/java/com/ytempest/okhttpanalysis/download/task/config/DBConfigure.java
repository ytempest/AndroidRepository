package com.ytempest.okhttpanalysis.download.task.config;

import android.os.Environment;

import java.io.File;

/**
 * @author ytempest
 *         Description：
 */
public class DBConfigure {
    /**
     * 保存了暂停任务对象的数据库的所在文件夹全路径
     */
    public static final String TASK_DATABASE_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "TaskDatabase";

    /**
     * 保存了暂停任务对象的数据库名称
     */
    public static final String TASK_DATABASE = "tasks.db";
}
