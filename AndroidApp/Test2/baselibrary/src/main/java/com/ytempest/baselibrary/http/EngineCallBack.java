package com.ytempest.baselibrary.http;

import android.content.Context;

import java.util.Map;

/**
 * @author ytempest
 *         Description: 自己的网络回调规范
 */
public interface EngineCallBack {
    /**
     * 开始执行，在执行之前会回调的方法
     */
    public void onPreExecute(Context context, Map<String, Object> params);

    /**
     * 进行网络连接失败后回调的方法
     *
     * @param e
     */
    public void onError(Exception e);


    /**
     * 请求网络成功后回调的方法
     *
     * @param result
     */
    public void onSuccess(String result);

    /**
     * 默认的回调
     */
    public final EngineCallBack DEFAULT_CALL_BACK = new EngineCallBack() {
        @Override
        public void onPreExecute(Context context, Map<String, Object> params) {
        }

        @Override
        public void onError(Exception e) {
        }

        @Override
        public void onSuccess(String result) {
        }
    };
}
