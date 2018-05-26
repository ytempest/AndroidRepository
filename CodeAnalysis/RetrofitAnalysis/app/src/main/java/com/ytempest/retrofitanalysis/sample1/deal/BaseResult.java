package com.ytempest.retrofitanalysis.sample1.deal;

/**
 * @author ytempest
 *         Description：底层的一些数据，这些数据的格式无论是请求成功或请求失败都不会改变
 */
public class BaseResult {
    private String code;
    private String msg;

    public String getCode() {
        return code;
    }

    public BaseResult setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public BaseResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public boolean isOk() {
        return "0011".equals(code);
    }
}
