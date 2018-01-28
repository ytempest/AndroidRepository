package com.ytempest.framelibrary.db;

import android.text.TextUtils;

import java.util.Locale;

/**
 * @author ytempest
 * Description: 数据库辅助类
 */
public class DaoUtils {

    private DaoUtils() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

	/**
	 * 返回clazz的实体名
	 */	
    public static String getTableName(Class<?> clazz) {
        return clazz.getSimpleName();
    }

	/**
	 * 根据传入的type类型字符串返回数据库的数据类型
	 */	
    public static String getColumnType(String type) {
        String value = null;
        if (type.contains("String")) {
            value = "text";
        } else if (type.contains("char")) {
            value = "varchar";
        } else if (type.contains("int")) {
            value = "integer";
        } else if (type.contains("long")) {
            value = "long";
        } else if (type.contains("boolean")) {
            value = "boolean";
        } else if (type.contains("float")) {
            value = "float";
        } else if (type.contains("double")) {
            value = "double";
        }  
        return value;
    }

	/**
	 * 将string的首字母转成大写
	 */
    public static String capitalize(String string) {
        if (!TextUtils.isEmpty(string)) {
            return string.substring(0, 1).toUpperCase(Locale.US) + string.substring(1);
        }
        return string == null ? null : "";
    }
}
