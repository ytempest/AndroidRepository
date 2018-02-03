package com.ytempest.framelibrary.view.binnerview;

import android.content.Context;
import android.os.Build;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * @author ytempest
 *         Description：用于改变 BannerViewPager 自动轮播速度，以及轮播速度的变化轨迹的一个类
 */
public class BannerScroller extends Scroller {
    /**
     * 设置ViewPager切换的速率 - 动画持续的时间
     */
    private int mScrollerDuration = 600;

    public BannerScroller(Context context) {
        this(context, null);
    }

    public BannerScroller(Context context, Interpolator interpolator) {
        this(context, interpolator,
                context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB);
    }

    public BannerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    /**
     * BannerViewPager 自动轮播切换页面的时候会调用这个方法
     *
     * @param duration 切换页面动画持续的时间
     */
    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mScrollerDuration);
    }

    /**
     * 设置切换页面持续的时间
     */
    public void setScrollerDuration(int scrollerDuration) {
        this.mScrollerDuration = scrollerDuration;
    }
}
