package com.ytempest.payment.callback;

/**
 * @author ytempest
 *         Description：
 */
public interface OnInputFinishListener {
    /**
     * 当密码输入完成后会调用该方法
     *
     * @param password 输入的密码
     */
    void onFinish(String password);
}
