package com.ytempest.framelibrary.db.curd;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.ytempest.framelibrary.db.DaoUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ytempest
 * @date 2017/3/7
 * Description: 专门用来查询的支持类
 */
public class QuerySupport<T> {
    /** 查询的列 */
    private String[] mQueryColumns;
    /** 查询的条件 */
    private String mQuerySelection;
    /** 查询的参数 */
    private String[] mQuerySelectionArgs;
    /** 查询分组 */
    private String mQueryGroupBy;
    /** 查询对结果集进行过滤 */
    private String mQueryHaving;
    /** 查询排序 */
    private String mQueryOrderBy;
    /** 查询可用于分页 */
    private String mQueryLimit;

    private Class<T> mClass;
    private SQLiteDatabase mSQLiteDatabase;

    public QuerySupport(SQLiteDatabase sqLiteDatabase, Class<T> clazz) {
        this.mClass = clazz;
        this.mSQLiteDatabase = sqLiteDatabase;
    }

    public QuerySupport columns(String... columns) {
        this.mQueryColumns = columns;
        return this;
    }

    public QuerySupport selectionArgs(String... selectionArgs) {
        this.mQuerySelectionArgs = selectionArgs;
        return this;
    }

    public QuerySupport having(String having) {
        this.mQueryHaving = having;
        return this;
    }

    public QuerySupport orderBy(String orderBy) {
        this.mQueryOrderBy = orderBy;
        return this;
    }

    public QuerySupport limit(String limit) {
        this.mQueryLimit = limit;
        return this;
    }

    public QuerySupport groupBy(String groupBy) {
        this.mQueryGroupBy = groupBy;
        return this;
    }

    public QuerySupport selection(String selection) {
        this.mQuerySelection = selection;
        return this;
    }

    public List<T> query() {
        Cursor cursor = mSQLiteDatabase.query(DaoUtils.getTableName(mClass), mQueryColumns, mQuerySelection,
                mQuerySelectionArgs, mQueryGroupBy, mQueryHaving, mQueryOrderBy, mQueryLimit);
        clearQueryParams();
        return cursorToList(cursor);
    }

    public List<T> queryAll() {
        Cursor cursor = mSQLiteDatabase.query(DaoUtils.getTableName(mClass), null, null, null, null, null, null);
        return cursorToList(cursor);
    }

    /**
     * 清空参数
     */
    private void clearQueryParams() {
        mQueryColumns = null;
        mQuerySelection = null;
        mQuerySelectionArgs = null;
        mQueryGroupBy = null;
        mQueryHaving = null;
        mQueryOrderBy = null;
        mQueryLimit = null;
    }

    /**
     * 通过Cursor封装成查找对象
     * @return 对象集合列表
     */
    private List<T> cursorToList(Cursor cursor) {
        List<T> list = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    // 1. 实例化一个T对象
                    T instance = mClass.newInstance();
                    Field[] fields = mClass.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        // 获取属性的名称
                        String fieldName = field.getName();
						// 获取属性的类型
                        Class<?> fieldType = field.getType();
                        // 获取角标
                        int index = cursor.getColumnIndex(fieldName);
                        if (index == -1) {
                            continue;
                        }
                        // 2. 通过反射获取不同类型的get方法 getInt(int)、getLong(int)、getString(int)
                        Method cursorMethod = cursorMethod(fieldType);
                        if (cursorMethod != null) {
                            // 2.1 执行get方法获取cursor中的数据
                            Object value = cursorMethod.invoke(cursor, index);
                            if (value == null) {
                                continue;
                            }
                            // 数据在数据库存储和在java中使用的时候，数据类型是不一样的
                            // 3. 将从数据库获取的数据转换成java的基本数据
                            value = transformSimpleData(fieldType, value);
                            // 4. 将数据注入到属性中
                            field.set(instance, value);
                        }
                    }
                    // 加入集合
                    list.add(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Nullable
    private Object transformSimpleData(Class<?> fieldType, Object finalValue) {
        if (fieldType == boolean.class || fieldType == Boolean.class) {
            if ("0".equals(String.valueOf(finalValue))) {
                finalValue = false;
            } else if ("1".equals(String.valueOf(finalValue))) {
                finalValue = true;
            }
        } else if (fieldType == char.class || fieldType == Character.class) {
            finalValue = ((String) finalValue).charAt(0);
        } else if (fieldType == Date.class) {
            long date = (long) finalValue;
            if (date <= 0) {
                finalValue = null;
            } else {
                finalValue = new Date(date);
            }
        }
        return finalValue;
    }

    /**
     * @param type 数据类型的Class
     * @return getXXX方法的Method
     * @throws Exception
     */
    private Method cursorMethod(Class<?> type) throws Exception {
        String methodName = getColumnMethodName(type);
        return Cursor.class.getMethod(methodName, int.class);
    }

    /**
     * 根据传入的数据类型，返回相应的getXXX方法的字符串（"getInt"、"getString"）
     * @param fieldType 数据类型的Class
     * @return "getXXX" 字符串
     */
    private String getColumnMethodName(Class<?> fieldType) {
        String typeName;
		// 1. 判断数据类型是否基本数据类型
        if (fieldType.isPrimitive()) {
			// 1.1 是基本数据类型就将首字母转成大写（boolean -> Boolean）
            typeName = DaoUtils.capitalize(fieldType.getName());
        } else {
            typeName = fieldType.getSimpleName();
        }
        // 2. 根据数据的类型不同拼接不同的getXXX方法
        String methodName = "get" + typeName;
        if ("getBoolean".equals(methodName)) {
            methodName = "getInt";
        } else if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
            methodName = "getString";
        } else if ("getDate".equals(methodName)) {
            methodName = "getLong";
        } else if ("getInteger".equals(methodName)) {
            methodName = "getInt";
        }
        return methodName;
    }
}
