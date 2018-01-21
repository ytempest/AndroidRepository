package com.ytempest.test2.mode;

/**
 * create by ytempest at 2017-11-20
 * Description:
 */
public class Test {

    /**
     * error_no : 101
     * error_code : ximalaya.common.signature_check_failed
     * error_desc : signature check failed
     */

    private int error_no;
    private String error_code;
    private String error_desc;

    public Test() {
    }

    public Test(int error_no, String error_code, String error_desc) {
        this.error_no = error_no;
        this.error_code = error_code;
        this.error_desc = error_desc;
    }

    public int getError_no() {
        return error_no;
    }

    public void setError_no(int error_no) {
        this.error_no = error_no;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getError_desc() {
        return error_desc;
    }

    public void setError_desc(String error_desc) {
        this.error_desc = error_desc;
    }
}
