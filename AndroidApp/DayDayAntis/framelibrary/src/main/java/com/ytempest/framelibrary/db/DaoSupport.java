package com.ytempest.framelibrary.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.ytempest.framelibrary.db.curd.QuerySupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author ytempest
 * @date 2017/3/5
 * Description:自己的数据库引擎
 */
public class DaoSupport<T> implements IDaoSupport<T> {
    private static final String TAG = "DaoSupport";

    private SQLiteDatabase mSqLiteDatabase;
    /** 进行操作的表的泛型类 */
    private Class<T> mClazz;
    private static final Object[] mPutMethodArgs = new Object[2];
    /** 存储ContentValues的put()方法以提高性能 */
    private static final Map<String, Method> mPutMethods = new ArrayMap<>();
	/** 查询的支持类 */
	private QuerySupport<T> mQuerySupport;

    /**
     * 初始化数据库引擎，如果clazz表不存在则创建
     * @param sqLiteDatabase 数据库实例
     * @param clazz 进行操作的表的Class对象
     */
    @Override
    public void init(SQLiteDatabase sqLiteDatabase, Class<T> clazz) {
        this.mSqLiteDatabase = sqLiteDatabase;
        this.mClazz = clazz;

        // 1. 拼接创建T类的表
        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("create table if not exists ")
                .append(DaoUtils.getTableName(mClazz))
                .append("(id integer primary key autoincrement, ");

        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            String fieldType = field.getType().getSimpleName();
            //  1.1 type需要转换成数据库的数据类型 int --> integer, String -->text;
            // 然后将每一个字段拼接到 createTableSql中
            createTableSql.append(fieldName).append(" ").append(DaoUtils.getColumnType(fieldType)).append(", ");
        }
        createTableSql.replace(createTableSql.length() - 2, createTableSql.length(), ")");

        String sql = createTableSql.toString();

        Log.e(TAG, "表语句--> " + sql);

        // 1.2 创建表
        mSqLiteDatabase.execSQL(sql);
    }


    /**
     * 插入数据库，obj是任意对象
     */
    @Override
    public long insert(T obj) {

        // 将obj封装成ContentValues
        ContentValues values = contentValuesByObj(obj);

        // 速度比第三方的快一倍左右
        return mSqLiteDatabase.insert(DaoUtils.getTableName(mClazz), null, values);
    }

    @Override
    public void insert(List<T> list) {
        // 批量插入采用 事物
        mSqLiteDatabase.beginTransaction();
        for (T data : list) {
            // 调用单条插入
            insert(data);
        }
        mSqLiteDatabase.setTransactionSuccessful();
        mSqLiteDatabase.endTransaction();
    }


    @Override
    public QuerySupport<T> getQuerySupport() {
        if(mQuerySupport == null){
            mQuerySupport = new QuerySupport<T>(mSqLiteDatabase,mClazz);
        }
        return mQuerySupport;
    }

    @Override
    public int delete(String whereClause, String...whereArgs) {
        return mSqLiteDatabase.delete(DaoUtils.getTableName(mClazz), whereClause, whereArgs);
    }

    @Override
    public int update(T obj, String whereClause, String... whereArgs) {
        ContentValues values = contentValuesByObj(obj);
        return mSqLiteDatabase.update(DaoUtils.getTableName(mClazz),
                values, whereClause, whereArgs);
    }


    /**
     * 将 obj 转成 ContentValues
     */
    private ContentValues contentValuesByObj(T obj) {
        ContentValues values = new ContentValues();

        Field[] fields = mClazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                // 1. put方法的第一个参数是列名，第二个参数是列名的类型
                // 1.1 获取put方法的列名
                String fieldName = field.getName();
                // 1.2 获取put方法的列名的类型
                Object fieldValue = field.get(obj);
                mPutMethodArgs[0] = fieldName;
                mPutMethodArgs[1] = fieldValue;

                // 2. 获取不同类型put方法 put(String,Integer),put(String,Float)
                // 2.1 获取属性的类型
                String filedTypeName = field.getType().getName();
                // 2.4 从缓存的ArrayMap中获取put方法
                Method putMethod = mPutMethods.get(filedTypeName);
                if (putMethod == null) {
                    // 2.2 获取符合属性类型的put方法
                    putMethod = ContentValues.class.getDeclaredMethod("put",
                            String.class, fieldValue.getClass());
                    // 2.3 缓存put方法
                    mPutMethods.put(filedTypeName, putMethod);
                }
                // 3. 通过反射执行不同类型的put方法
                putMethod.invoke(values, mPutMethodArgs);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mPutMethodArgs[0] = null;
                mPutMethodArgs[1] = null;
            }
        }
        return values;
    }

    // 结合到
    // 1. 网络引擎的缓存
    // 2. 资源加载的源码NDK
}
