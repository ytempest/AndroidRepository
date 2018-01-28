package com.ytempest.baselibrary.http;

import android.content.Context;

import java.util.Map;

/**
 * @author ytempest
 *         Description:  自己的网络请求的引擎的规范
 */
public interface IHttpEngine {

    /**
     * get请求
     */
    public void get(boolean cache, Context context, String url, Map<String, Object> params, EngineCallBack callBack);

    /**
     * post请求
     */
    public void post(boolean cache, Context context, String url, Map<String, Object> params, EngineCallBack callBack);

    // 下载文件

    // 上传文件

    // https 添加证书
}
