package com.ytempest.baselibrary.http;

import android.content.Context;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ytempest
 *         Description:  自己的一套实现切换网络引擎的网络请求工具类
 */
public class HttpUtils {

    /**
     * 网络请求的网址
     */
    private String mUrl;
    private static final int POST_TYPE = 0x0011;
    private static final int GET_TYPE = 0x0022;
    /**
     * 请求方式
     */
    private int mType = GET_TYPE;
    private Map<String, Object> mParams;
    /**
     * 默认网络引擎为OkHttpEngine
     */
    private static IHttpEngine mHttpEngine = null;
    private Context mContext;
    /**
     * 是否读取缓存
     */
    private boolean mCache = false;


    private HttpUtils(Context context) {
        mContext = context;
        mParams = new HashMap<>();
    }

    /**
     * 在Application初始化引擎
     *
     * @param httpEngine 满足需求的网络引擎
     */
    public static void init(IHttpEngine httpEngine) {
        mHttpEngine = httpEngine;
    }


    /**
     * 链式调用的第一节
     *
     * @param context
     */
    public static HttpUtils with(Context context) {
        return new HttpUtils(context);
    }

    public HttpUtils url(String url) {
        this.mUrl = url;
        return this;
    }

    public HttpUtils post() {
        mType = POST_TYPE;
        return this;
    }

    public HttpUtils get() {
        mType = GET_TYPE;
        return this;
    }

    /**
     * 是否配置缓存
     */
    public HttpUtils cache(boolean isCache) {
        mCache = isCache;
        return this;
    }

    /**
     * 添加参数
     */
    public HttpUtils addParam(String key, Object value) {
        mParams.put(key, value);
        return this;
    }

    public HttpUtils addParams(Map<String, Object> params) {
        mParams.putAll(params);
        return this;
    }

    /**
     * 切换网路引擎
     */
    public HttpUtils exchangeEngine(IHttpEngine httpEngine) {
        mHttpEngine = httpEngine;
        return this;
    }

    // 请求头：当有需要时可自行添加头部

    public void execute() {
        execute(null);
    }

    /**
     * 添加自己的回调规范，并执行网络请求
     */
    public void execute(EngineCallBack callBack) {

        if (callBack == null) {
            callBack = EngineCallBack.DEFAULT_CALL_BACK;
        }

        // 每次执行都会进入这个方法：这里添加是行不通的
        // 1.baseLibrary里面这里面不包含业务逻辑
        // 2.如果有多条业务线，
        // 让callBack回调去
        callBack.onPreExecute(mContext, mParams);

        // 判断执行方法
        if (mType == POST_TYPE) {
            post(mUrl, mParams, callBack);
        }

        if (mType == GET_TYPE) {
            get(mUrl, mParams, callBack);
        }
    }

    private void get(String url, Map<String, Object> params, EngineCallBack callBack) {
        mHttpEngine.get(mCache, mContext, url, params, callBack);
    }


    private void post(String url, Map<String, Object> params, EngineCallBack callBack) {
        mHttpEngine.get(mCache, mContext, url, params, callBack);
    }

    /**
     * 拼接参数
     */
    public static String jointParams(String url, Map<String, Object> params) {
        if (params == null || params.size() <= 0) {
            return url;
        }

        StringBuilder newUrl = new StringBuilder(url);
        if (!url.contains("?")) {
            newUrl.append("?");
        } else {
            if (!url.endsWith("?")) {
                newUrl.append("&");
            }
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            newUrl.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        // 删除请求网址的最后一个字符&
        newUrl.deleteCharAt(newUrl.length() - 1);

        return newUrl.toString();
    }

    /**
     * 解析一个类上面的class信息
     */
    public static Class<?> analysisClazzInfo(Object object) {
        Type genType = object.getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        return (Class<?>) params[0];
    }


}
