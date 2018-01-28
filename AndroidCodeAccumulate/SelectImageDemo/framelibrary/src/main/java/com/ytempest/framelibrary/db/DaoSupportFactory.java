package com.ytempest.framelibrary.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

/**
 * @author ytempest
 * Description:数据库引擎工厂
 */
public class DaoSupportFactory {

    private static DaoSupportFactory mFactory;

    /** 持有外部数据库的引用 */
    private SQLiteDatabase mSqLiteDatabase;

    private DaoSupportFactory() {
        // 判断是否有存储卡，有则把数据库放到内存卡里面；不放在内部存储，6.0要动态申请权限
        File dbRoot = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + File.separator + "nhdz" + File.separator + "database");
        if (!dbRoot.exists()) {
            dbRoot.mkdirs();
        }
        File dbFile = new File(dbRoot, "nhdz.db");

        // 打开或者创建一个数据库
        mSqLiteDatabase = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
    }

    public static DaoSupportFactory getFactory() {
        if (mFactory == null) {
            synchronized (DaoSupportFactory.class) {
                if (mFactory == null) {
                    mFactory = new DaoSupportFactory();
                }
            }
        }
        return mFactory;
    }

    public <T> IDaoSupport<T> getDao(Class<T> clazz) {
        IDaoSupport<T> daoSupport = new DaoSupport<>();
        // 初始化数据库引擎以及clazz表
        daoSupport.init(mSqLiteDatabase,clazz);
        return daoSupport;
    }
}
