package com.ytempest.baselibrary.view.indicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.ytempest.baselibrary.R;


/**
 * @author ytempest
 *         Description:  ViewPager的指示器
 */
public class TrackIndicatorView extends HorizontalScrollView implements ViewPager.OnPageChangeListener {
    /**
     * Indicator适配器
     */
    private IndicatorAdapter mIndicatorAdapter;

    /**
     * 存放并管理条目的容器
     */
    private IndicatorGroupView mIndicatorGroup;

    /**
     * 指定一屏幕可显示多少个条目
     */
    private int mTabVisibleNum = 0;

    /**
     * 条目的宽度
     */
    private int mItemWidth;

    private ViewPager mViewPager;

    /**
     * 标识当前显示的条目的位置
     */
    private int mCurrentPosition = 0;
    /**
     * 解决移动到指定条目时，下标抖动的问题
     */
    private boolean mIsExecuteScroll = false;

    /**
     * 是否平滑移动
     */
    private boolean mSmoothScroll;


    public TrackIndicatorView(Context context) {
        this(context, null);
    }

    public TrackIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrackIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mIndicatorGroup = new IndicatorGroupView(context);
        // HorizontalScrollView只能添加一个子View
        addView(mIndicatorGroup);

        // 初始化自定义属性
        initAttribute(context, attrs);
    }

    /**
     * 初始化自定义属性
     */
    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TrackIndicatorView);
        // 如果没有指定 tabVisibleNum 就默认使用 mTabVisibleNum
        mTabVisibleNum = array.getInt(R.styleable.TrackIndicatorView_tabVisibleNum, mTabVisibleNum);
        // 记得回收
        array.recycle();
    }

    /**
     * 设置适配器
     */
    public void setAdapter(IndicatorAdapter adapter, ViewPager viewPager) {
        setAdapter(adapter, viewPager, true);
    }

    /**
     * 设置适配器
     *
     * @param smoothScroll 是否平滑移动
     */
    public void setAdapter(IndicatorAdapter adapter, ViewPager viewPager, boolean smoothScroll) {
        if (viewPager == null) {
            throw new NullPointerException("ViewPager is null!");
        }
        this.mSmoothScroll = smoothScroll;
        mViewPager = viewPager;
        // 给 ViewPager 添加滑动监听
        mViewPager.addOnPageChangeListener(this);
        setAdapter(adapter);
    }

    /**
     * 设置一个适配器
     */
    public void setAdapter(IndicatorAdapter adapter) {
        if (adapter == null) {
            throw new NullPointerException("indicatorAdapter is null!");
        }

        this.mIndicatorAdapter = adapter;

        int itemCount = mIndicatorAdapter.getCount();
        // 循环添加条目到 IndicatorGroup容器中进行管理
        for (int i = 0; i < itemCount; i++) {
            View itemView = mIndicatorAdapter.getView(i, mIndicatorGroup);
            mIndicatorGroup.addItemView(itemView);

            // 如果有 ViewPager,就实现条目的点击逻辑
            if (mViewPager != null) {
                initItemClick(itemView, i);
            }
        }

        // 点击或者切换的时候改变状态 默认点亮第一个位置，把第一个位置
        // 的View传递过去，让实现 IndicatorAdapter的子类实现对view 的点亮（颜色的设置）
        mIndicatorAdapter.highLightIndicator(mIndicatorGroup.getItemAt(0));
    }

    /**
     * 实现条目的点击逻辑
     *
     * @param itemView 条目的View
     * @param position 条目的位置
     */
    private void initItemClick(View itemView, final int position) {
        itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到指定的 ViewPager
                mViewPager.setCurrentItem(position, mSmoothScroll);
                // 移动 指示器IndicatorView 到指定位置
                smoothScrollIndicator(position);
                // 移动下标到指定位置
                mIndicatorGroup.scrollBottomTrack(position);
            }
        });
    }

    /**
     * 平滑移动 指示器IndicatorView
     */
    private void smoothScrollIndicator(int position) {
        // 当前总共的位置
        float totalScroll = (position) * mItemWidth;
        // 如果指示器宽度小于ItemView，就应该让指示器居中，下面计算左边的位移量
        int offsetScroll = (getWidth() - mItemWidth) / 2;
        // 最终的一个偏移量
        final int finalScroll = (int) (totalScroll - offsetScroll);
        // 调用ScrollView自带带动画的方法，滚动到坐标为（x，y）位置，第一个参数是x，第二个参数是y
        smoothScrollTo(finalScroll, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed && mItemWidth == 0) {
            // 指定子Item的宽度
            mItemWidth = getItemWidth();
            // 循环指定子Item的宽度
            for (int i = 0; i < mIndicatorAdapter.getCount(); i++) {
                mIndicatorGroup.getItemAt(i).getLayoutParams().width = mItemWidth;
            }

            // 添加底部跟踪的指示器
            mIndicatorGroup.addBottomTrackView(mIndicatorAdapter.getBottomTrackView(), mItemWidth);
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mIsExecuteScroll) {
            // 滑动的时候会不断的调用
            scrollCurrentIndicator(position, positionOffset);
            mIndicatorGroup.scrollBottomTrack(position, positionOffset);
            // 如果是点击就不要执行onPageScrolled这个方法
        }
    }


    /**
     * 滑动过程如果能滑动到新的 ViewPager 就会调用这个方法
     *
     * @param position 当前位置的ViewPager的位置
     */
    @Override
    public void onPageSelected(int position) {
        // 上一个位置的 ViewPager 重置，将当前位置点亮，
        mIndicatorAdapter.restoreIndicator(mIndicatorGroup.getItemAt(mCurrentPosition));
        // 记录当前ViewPager的位置
        mCurrentPosition = position;
        // 将当前位置的ViewPager点亮
        mIndicatorAdapter.highLightIndicator(mIndicatorGroup.getItemAt(mCurrentPosition));
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING) {
            mIsExecuteScroll = true;
        }

        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mIsExecuteScroll = false;
        }
    }

    /**
     * 获取条目的宽度
     */
    public int getItemWidth() {
        // 获取 TrackIndicatorView 的宽度
        int parentWidth = getWidth();
        if (mTabVisibleNum != 0) {
            return parentWidth / mTabVisibleNum;
        }

        // 指定子item的宽度
        int itemWidth = 0;

        int maxItemWidth = 0;

        // 遍历所有条目找出宽度最大的条目
        for (int i = 0; i < mIndicatorAdapter.getCount(); i++) {
            int currentItemWidth = mIndicatorGroup.getItemAt(i).getMeasuredWidth();
            maxItemWidth = Math.max(currentItemWidth, maxItemWidth);
        }
        itemWidth = maxItemWidth;

        int allWidth = mIndicatorAdapter.getCount() * itemWidth;
        // 如果所有条目宽度相加小于一屏幕，就重新分配条目宽度
        if (allWidth < parentWidth) {
            itemWidth = parentWidth / mIndicatorAdapter.getCount();
        }
        return itemWidth;
    }

    /**
     * 不断的滚动当前的指示器
     */
    private void scrollCurrentIndicator(int position, float positionOffset) {
        // 当前总共的位置
        float totalScroll = (position + positionOffset) * mItemWidth;
        // 左边的偏移
        int offsetScroll = (getWidth() - mItemWidth) / 2;
        // 最终的一个偏移量
        final int finalScroll = (int) (totalScroll - offsetScroll);
        // 调用ScrollView自带的方法
        scrollTo(finalScroll, 0);
    }
}
