package com.ytempest.payment;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.ytempest.payment.callback.OnInputFinishListener;
import com.ytempest.payment.callback.OnKeyboardClickListener;


/**
 * @author ytempest
 *         Description：一个能完成输入密码的View
 */
public class PayView extends LinearLayout {

    private PasswordEditText mPasswordEditText;

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
        mCloseView = findViewById(R.id.pay_view_close);
        KeyBoard keyBoard = findViewById(R.id.pay_view_keyboard);

        keyBoard.setOnKeyboardClickListener(new OnKeyboardClickListener() {
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

    public void setOnClosePayViewListener(OnClosePayViewListener listener) {
        mCloseView.setOnClickListener(listener);
    }


    /**
     * 点击PayView的关闭按钮会回调该接口
     */
    public interface OnClosePayViewListener extends OnClickListener {
    }

}
