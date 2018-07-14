package com.ytempest.framelibrary.net.response;

import okhttp3.Callback;

/**
 * @author ytempest
 *         Description：
 */
public abstract class BaseCallback implements Callback {

    /******   这里定义返回数据的状态码，这个由服务器指定  *****/
    // 请求状态码
    public static final String RESULT_CODE = "code";
    // 请求成功
    public static final int RESULT_OK = 1;
    public static final String ERROR_MSG = "emsg";
    // 空数据
    public static final String EMPTY_MSG = "";
    public final String COOKIE_STORE = "Set-Cookie";


    /******   这里定义异常状态码  *****/
    // 网络异常
    public static final int NETWORK_ERROR = -1;
    // 超时
    public static final int TIME_OUT = -2;
    // 其他错误
    public static final int OTHER_CODE = -3;
    // JSON错误
    public static final int JSON_ERROR = -4;
}
