package com.ytempest.test2;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;

import java.lang.reflect.Field;

/**
 * @author ytempest
 *         Description:
 */
public class TabLayoutPro extends TabLayout {
    private int mTabViewNumber = 6;
    private static final String SCROLLABLE_TAB_MIN_WIDTH = "mScrollableTabMinWidth";

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
        initTabMinWidth();
        // 一定要对该资源进行回收
        array.recycle();
    }

    private void initTabMinWidth() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int tabMinWidth = screenWidth / mTabViewNumber;

        Field field;
        try {
            field = TabLayout.class.getDeclaredField(SCROLLABLE_TAB_MIN_WIDTH);
            field.setAccessible(true);
            field.set(this, tabMinWidth);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}