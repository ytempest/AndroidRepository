package com.ytempest.baselibrary.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author ytempest
 *         Description：可以设置是否可以左右活动的ViewPager
 */
public class NotScrollViewPager extends ViewPager {

    private boolean isSlideEnable = false;

    public NotScrollViewPager(Context context) {
        super(context);
    }

    public NotScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSlideEnable && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isSlideEnable && onTouchEvent(ev);

    }

    public void setSlideEnable(boolean enable) {
        isSlideEnable = enable;
    }
}
