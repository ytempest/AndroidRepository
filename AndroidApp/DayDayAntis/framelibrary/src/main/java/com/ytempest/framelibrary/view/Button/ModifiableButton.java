package com.ytempest.framelibrary.view.Button;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

import com.ytempest.framelibrary.R;

/**
 * @author ytempest
 *         Description：这是一个可以切换按钮状态的一个自定义按钮
 */
public class ModifiableButton extends Button {

    /**
     * 按钮不可用时的文字颜色
     */
    protected int mDisableTextColor = Color.parseColor("#FFE8F0EC");
    /**
     * 按钮不可用时的背景
     */
    protected int mDisableBackgroundRes = 0;

    protected Drawable mNormalBackground;
    protected ColorStateList mNormalTextColor;
    protected CharSequence mNormalText;


    public ModifiableButton(Context context) {
        super(context);
        initOriginParams();
    }

    public ModifiableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initOriginParams();
        initAttrs(context, attrs);
    }

    public ModifiableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initOriginParams();
        initAttrs(context, attrs);
    }

    private void initOriginParams() {
        mNormalText = getText();
        mNormalTextColor = getTextColors();
        mNormalBackground = getBackground();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ModifiableButton);

        mDisableTextColor = attributes.getColor(R.styleable.ModifiableButton_disable_text_color, mDisableTextColor);

        mDisableBackgroundRes = attributes.getResourceId(R.styleable.ModifiableButton_disable_bg, mDisableBackgroundRes);

        attributes.recycle();
    }


    @SuppressLint("NewApi")
    public void switchNormalStatus() {
        setEnabled(true);
        setText(mNormalText);
        setTextColor(mNormalTextColor);
        setBackground(mNormalBackground);
    }

    public void switchDisableStatus() {
        setEnabled(false);
        setTextColor(mDisableTextColor);
        setBackgroundResource(mDisableBackgroundRes);
    }
}
