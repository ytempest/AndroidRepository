package com.ytempest.payment;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;


/**
 * @author ytempest
 *         Description：一个能完成输入密码的View
 */
public class PayView extends LinearLayout {

    private PasswordEditText mPasswordEditText;

    private PasswordKeyBoard mPasswordKeyBoard;

    private View mCloseView;

    public PayView(Context context) {
        this(context, null);
    }

    public PayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.ui_pay_view, this);

        mPasswordEditText = findViewById(R.id.pay_view_input);
        mPasswordKeyBoard = findViewById(R.id.pay_view_keyboard);
        mCloseView = findViewById(R.id.pay_view_close);

        mPasswordKeyBoard.setOnKeyboardClickListener(new OnKeyboardClickListener() {
            @Override
            public void onNumberClick(String number) {
                mPasswordEditText.inputPassword(number);
            }

            @Override
            public void onDelete() {
                mPasswordEditText.deleteLastPassword();
            }
        });

    }


    public void setOnInputFinishListener(OnInputFinishListener listener) {
        mPasswordEditText.setOnInputFinishListener(listener);
    }

    public void setOnClosePayViewListener(OnClickListener listener) {
        mCloseView.setOnClickListener(listener);
    }


    public interface OnInputFinishListener {
        /**
         * 当密码输入完成后会调用该方法
         *
         * @param password 输入的密码
         */
        void onFinish(String password);
    }


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

    /**
     * 点击PayView的关闭按钮会回调该接口
     */
    public interface OnClosePayViewListener extends OnClickListener {
    }

}
