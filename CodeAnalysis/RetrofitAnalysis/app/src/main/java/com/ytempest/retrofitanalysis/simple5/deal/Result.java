package com.ytempest.retrofitanalysis.simple5.deal;

import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static android.content.ContentValues.TAG;

/**
 * @author ytempest
 *         Description：这个类可以匹配后台返回的所有数据，无论是成功还是失败
 *         泛型T：用于标识后台返回成功数据时，data应该要转换的类型
 */
public class Result<T> extends BaseResult {
    /**
     * 使用了 Object，所以无论后台返回什么样的格式（空字符串、对象）都可以匹配
     */
    private T data;

    public T getData() {
        return data;
    }

    public Result setData(T data) {
        this.data = data;
        return this;
    }
}
