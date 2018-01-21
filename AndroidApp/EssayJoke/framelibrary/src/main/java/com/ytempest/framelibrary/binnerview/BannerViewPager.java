package com.ytempest.framelibrary.binnerview;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：能不断自动轮播的 ViewPager，提供启动、停止，重启、停止自动轮播的功能
 */
public class BannerViewPager extends ViewPager {

    private BannerAdapter mBannerAdapter;
    /**
     * 启动轮播的消息标志符
     */
    private final int SCROLL_MSG = 0x0011;

    /**
     * 页面切换间隔时间
     */
    private int mCutDownTime = 4000;
    /**
     * 自定义的页面切换的Scroller：用于改变ViewPager切换的速率
     */
    private BannerScroller mBannerScroller;

    private Handler mHandler;

    /**
     * 内存优化 --> 当前Activity
     */
    private Activity mCurActivity;
    /**
     * 内存优化 --> 缓存View，然后复用View
     */
    private List<View> mConvertViews;
    /**
     * 是否可以滚动
     */
    private boolean mScrollAble = true;
    /**
     * BannerViewPager 页面的回调监听
     */
    private BannerItemClickListener mBannerItemClickListener;

    /**
     * 监听Activity的生命周期以控制轮播的重启与暂停
     */
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks =
            new DefaultActivityLifecycleCallbacks() {
                /**
                 * 当当前的 Activity 进入 resumed 状态时会被调用
                 */
                @Override
                public void onActivityResumed(Activity activity) {
                    // 如果是 BannerViewPager 所在的Activity就开启轮播
                    if (activity == mCurActivity) {
                        resumeRoll();
                    }
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    if (activity == mCurActivity) {
                        // 暂停轮播
                        pauseRoll();
                    }
                }
            };
    /**
     * 标志 ViewPager 自动轮播是否暂停
     */
    private boolean isPause = false;


    public BannerViewPager(Context context) {
        this(context, null);
    }

    public BannerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCurActivity = (Activity) context;

        try {
            // 通过放射设置 ViewPager的 mBannerScroller 以改变 BannerViewPager 页面切换的速度
            Field field = ViewPager.class.getDeclaredField("mScroller");
            // 实例化 mBannerScroller，并设置插值器
            mBannerScroller = new BannerScroller(context, new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    return 1.0f - (1.0f - input) * (1.0f - input);
                }
            });
            field.setAccessible(true);
            field.set(this, mBannerScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mConvertViews = new ArrayList<>();

        initHandler();
    }

    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 每隔 指定的时间 后切换到下一页
                setCurrentItem(getCurrentItem() + 1);
                // 不断循环执
                startRoll();
            }
        };
    }

    /**
     * 设置 BannerViewPager 页面切换动画的持续时间
     */
    public void setScrollerDuration(int scrollerDuration) {
        mBannerScroller.setScrollerDuration(scrollerDuration);
    }

    /**
     * 设置自定义的 BannerAdapter
     */
    public void setBannerAdapter(BannerAdapter adapter) {
        this.mBannerAdapter = adapter;
        // 设置父类 ViewPager 的adapter
        setAdapter(new BannerPagerAdapter());
    }

    /**
     * 开启自动轮播
     */
    public void startRoll() {
        // adapter不能是空
        if (mBannerAdapter == null) {
            return;
        }

        // 判断是不是只有一条数据
        mScrollAble = mBannerAdapter.getCount() != 1;

        if (mScrollAble && mHandler != null) {
            // 清除消息
            mHandler.removeMessages(SCROLL_MSG);
            // 消息  延迟时间  让用户自定义  有一个默认  3500
            mHandler.sendEmptyMessageDelayed(SCROLL_MSG, mCutDownTime);
        }
    }

    /**
     * 停止自动轮播，清除消息以及 Handler
     */
    public void stopRoll() {
        // 销毁Handler的生命周期
        mHandler.removeMessages(SCROLL_MSG);
        mHandler = null;
    }


    /**
     * 继续自动轮播
     */
    public void resumeRoll() {
        isPause = false;
        mHandler.sendEmptyMessageDelayed(SCROLL_MSG, mCutDownTime);
    }

    /**
     * 暂停自动轮播
     */
    public void pauseRoll() {
        if (isPause) {
            return;
        }
        mHandler.removeMessages(SCROLL_MSG);
        isPause = true;
    }


    @Override
    protected void onAttachedToWindow() {
        if (mBannerAdapter != null) {
            initHandler();
            startRoll();
            // 管理Activity的生命周期
            mCurActivity.getApplication().registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        }
        super.onAttachedToWindow();
    }

    /**
     * 当 ViewPager 被销毁的时候会调用该方法，在这里销毁Handler停止发送  解决内存泄漏
     */
    @Override
    protected void onDetachedFromWindow() {
        if (mHandler != null) {
            // 解除绑定
            mCurActivity.getApplication().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
            stopRoll();
        }
        super.onDetachedFromWindow();
    }


    /**
     * 从缓存中获取用户定义的View作为复用界面
     */
    public View getConvertView() {
        for (int i = 0; i < mConvertViews.size(); i++) {
            // 由于已经从ViewPager移除，所以用户定义的View没有依附于任何父布局
            if (mConvertViews.get(i).getParent() == null) {
                return mConvertViews.get(i);
            }
        }
        return null;
    }


    public void setOnBannerItemClickListener(BannerItemClickListener listener) {
        this.mBannerItemClickListener = listener;
    }

    /**
     * Description：自定义 BannerPagerAdapter 适配器，实现无限轮播，并将用户自定义的View加载到ViewPager中
     */
    private class BannerPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // 设置 ViewPager 的页面数量以实现无限轮播
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            // 官方推荐这么写  源码
            return view == object;
        }

        /**
         * 创建ViewPager页面时会回调该方法，在这里实现将用户自定义的View填充到 ViewPager中
         *
         * @param container 就是我们的 BannerViewPager
         * @param position  BannerViewPager 的位置 --> 0 - Integer.MAX_VALUE
         * @return 用户自定义的 View
         */
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            // 根据真正的页面位置和缓冲的View 获取用户定义的 View
            View bannerItemView = mBannerAdapter.getView(position % mBannerAdapter.getCount(), getConvertView());
            // 添加ViewPager里面
            container.addView(bannerItemView);
            bannerItemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 回调点击监听
                    if (mBannerItemClickListener != null) {
                        mBannerItemClickListener.onPageClick(position % mBannerAdapter.getCount());
                    }
                }
            });
            return bannerItemView;
        }

        /**
         * BannerViewPager的页面移出屏幕后被销毁时会回调这个方法
         *
         * @param container ViewPager 对象
         * @param position  要被移除页面的位置
         * @param object    这个是 instantiateItem() 方法返回的对象
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            // 将从 ViewPager 中移出的 View缓存，以便复用
            mConvertViews.add((View) object);
        }
    }


    /**
     * Description：BannerViewPager 页面的监听器
     */
    public interface BannerItemClickListener {
        public void onPageClick(int position);
    }


}

