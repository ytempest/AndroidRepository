package com.ytempest.framelibrary.db;

import android.database.sqlite.SQLiteDatabase;

import com.ytempest.framelibrary.db.curd.QuerySupport;

import java.util.List;

/**
 * @author ytempest
 */
public interface IDaoSupport<T> {

    /**
     * 初始化数据库引擎
     * @param sqLiteDatabase 数据库实例
     * @param clazz 进行操作的表的Class对象
     */
    void init(SQLiteDatabase sqLiteDatabase, Class<T> clazz);

    /**
     * 插入数据
     */
    long insert(T t);

    /**
     * 批量插入
     */
    void insert(List<T> list);

    /**
     * 获取专门查询的支持类
     */
    QuerySupport<T> getQuerySupport();

    int delete(String whereClause, String... whereArgs);

    int update(T obj, String whereClause, String... whereArgs);
}
