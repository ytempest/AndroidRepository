package com.ytempest.framelibrary.net.exception;

/**
 * @author ytempest
 *         Description：
 */
public class OkHttpException extends Exception {
    private static final long serialVersionUID = 1L;

    private int errorCode;
    private String errorMsg;

    public OkHttpException(int errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
        this.errorMsg = msg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

}
