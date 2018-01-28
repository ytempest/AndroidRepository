package com.ytempest.framelibrary.http;

import android.content.Context;

import com.google.gson.Gson;
import com.ytempest.baselibrary.http.EngineCallBack;
import com.ytempest.baselibrary.http.HttpUtils;

import java.util.Map;

/**
 * @author ytempest
 * Description: 继承底层的回调方法，实现一些业务逻辑一直需要使用的东西
 */
public abstract class HttpCallBack<T> implements EngineCallBack {
    @Override
    public void onPreExecute(Context context, Map<String, Object> params) {
        // 大大方方的添加公用参数  与项目业务逻辑有关
		// 这里的参数是写死了，而在实际开发中需要动态获取
        // 项目名称  context
        params.put("app_name","joke_essay");
        params.put("version_name","5.7.0");
        params.put("ac","wifi");
        params.put("device_id","30036118478");
        params.put("device_brand","Xiaomi");
        params.put("update_version_code","5701");
        params.put("manifest_version_code","570");
        params.put("longitude","113.000366");
        params.put("latitude","28.171377");
        params.put("device_platform","android");

        // 调用业务层的回调方法
        onPreExecute();
    }

    /**
     * 业务层进行重写，在开启连接前调用
     */
    public abstract void onPreExecute();

    @Override
    public void onSuccess(String result) {

        Gson gson = new Gson();
        // data:{"name","darren"}   data:"请求失败"
        T objResult = (T) gson.fromJson(result, HttpUtils.analysisClazzInfo(this));

        // 调用业务层的回调方法
        onSuccess(objResult);
    }

    /**
     * 返回可以直接操作的对象
     * @param result 可以直接操作的对象
     */
    public abstract void onSuccess(T result);
}
