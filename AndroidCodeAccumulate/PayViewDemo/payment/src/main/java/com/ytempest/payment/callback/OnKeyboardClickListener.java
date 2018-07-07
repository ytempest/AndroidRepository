package com.ytempest.payment.callback;

/**
 * @author ytempest
 *         Description：
 */
public interface OnKeyboardClickListener {
    /**
     * 当点击了键盘上的某一个数字时会调用该方法
     *
     * @param number 当前点击的数字
     */
    void onNumberClick(String number);

    /**
     * 当点击了删除按钮的时候会调用该方法
     */
    void onDelete();
}
