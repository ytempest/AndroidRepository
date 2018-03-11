package com.ytempest.payment;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author ytempest
 *         Description：自定义密码输入键盘
 */
public class PasswordKeyBoard extends LinearLayout implements View.OnClickListener {

    /**
     * 键盘的按键监听器
     */
    private PayView.OnKeyboardClickListener mOnKeyboardClickListener;

    public PasswordKeyBoard(Context context) {
        this(context, null);
    }

    public PasswordKeyBoard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordKeyBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.ui_password_keyboard, this);

        setOnItemClickListener(this);
    }

    /**
     * 递归设置键盘每一个键的点击事件
     */
    public void setOnItemClickListener(View view) {
        if (view instanceof ViewGroup) {
            // 如果是ViewGroup则递归设置子View的点击事件
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setOnItemClickListener(viewGroup.getChildAt(i));
            }
        } else {
            // 如果是View就设置点击事件
            view.setOnClickListener(this);
        }
    }

    /**
     * 处理点击事件
     */
    @Override
    public void onClick(View view) {
        if (view instanceof TextView) {
            // 如果点击的是键盘的数字键
            TextView textView = (TextView) view;
            String number = textView.getText().toString();
            if (mOnKeyboardClickListener != null && !TextUtils.isEmpty(number)) {
                mOnKeyboardClickListener.onNumberClick(number);
            }
        } else if (view instanceof ImageView) {
            // 如果点击的是键盘的删除键
            if (mOnKeyboardClickListener != null) {
                mOnKeyboardClickListener.onDelete();
            }
        }
    }

    /**
     * 设置键盘的按键监听器
     */
    public void setOnKeyboardClickListener(PayView.OnKeyboardClickListener listener) {
        this.mOnKeyboardClickListener = listener;
    }

}
