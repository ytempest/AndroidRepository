package com.ytempest.studentmanage.model;

/**
 * @author ytempest
 *         Description：
 */
public class CommonResult {

    /**
     * msg : 成功
     * code : 0
     */
    public CommonResult() {
    }

    protected String msg;
    protected int code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "CommonResult{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                '}';
    }
}
