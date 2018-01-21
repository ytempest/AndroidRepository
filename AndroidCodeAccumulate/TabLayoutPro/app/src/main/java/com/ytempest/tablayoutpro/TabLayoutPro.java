package com.ytempest.tablayoutpro;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;

import java.lang.reflect.Field;

/**
 * @author ytempest
 *         Description: 新增可以指定条目的功能
 */
public class TabLayoutPro extends TabLayout {
    private static final String TAG = "TabLayout";
    /**
     * TayLayout显示条目
     */
    private int mTabViewNumber = -1;
    private static final String SCROLLABLE_TAB_MIN_WIDTH = "mScrollableTabMinWidth";
    private static final String MODE = "mMode";

    public TabLayoutPro(Context context) {
        this(context, null);
    }

    public TabLayoutPro(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayoutPro(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TabLayoutPro);
        mTabViewNumber = array.getInteger(R.styleable.TabLayoutPro_tabVisibleNum, mTabViewNumber);
        if (mTabViewNumber != -1) {
            // 如果 TayLayout 指定了显示的条目，就显示指定条目，同时 TayLayout为滚动模式
            initTabMinWidth();
        }
        array.recycle();
    }

    /**
     * 通过反射设置显示TayLayout显示条目和滚动模式
     */
    private void initTabMinWidth() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int tabMinWidth = screenWidth / mTabViewNumber;

        Field widthField;
        Field modeField;
        try {
            modeField = TabLayout.class.getDeclaredField(MODE);
            modeField.setAccessible(true);
            modeField.set(this, MODE_SCROLLABLE);
            widthField = TabLayout.class.getDeclaredField(SCROLLABLE_TAB_MIN_WIDTH);
            widthField.setAccessible(true);
            widthField.set(this, tabMinWidth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

