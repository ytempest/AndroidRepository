package com.ytempest.baselibrary.indicator;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * @author ytempest
 *         Description: indicator 的容器，包含itemView和底部跟踪的指示器
 */
public class IndicatorGroupView extends FrameLayout {

    /**
     * 管理的条目的 LinearLayout容器
     */
    private LinearLayout mIndicatorGroup;
    /**
     * 下标的View
     */
    private View mBottomTrackView;
    /**
     * 一个条目的宽度
     */
    private int mItemWidth;
    /**
     * 底部下标的LayoutParams
     */
    LayoutParams mTrackParams;

    /**
     * 下标位置居中后与左边的距离
     */
    private int mOriginLeftMargin;

    public IndicatorGroupView(Context context) {
        this(context, null);
    }

    public IndicatorGroupView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化管理条目的 LinearLayout容器
        mIndicatorGroup = new LinearLayout(context);
        // 添加 LinearLayout容器到 IndicatorGroupView中
        addView(mIndicatorGroup);
    }


    /**
     * 添加条目到 LinearLayout容器
     */
    public void addItemView(View itemView) {
        mIndicatorGroup.addView(itemView);
    }

    /**
     * 获取当前位置的条目
     */
    public View getItemAt(int position) {
        return mIndicatorGroup.getChildAt(position);
    }

    /**
     * 添加底部跟踪的下标到 LinearLayout容器中，并保存条目的宽度
     *
     * @param bottomTrackView 下标的View
     * @param itemWidth       条目的宽度
     */
    public void addBottomTrackView(View bottomTrackView, int itemWidth) {
        if (bottomTrackView == null) {
            return;
        }
        this.mItemWidth = itemWidth;

        this.mBottomTrackView = bottomTrackView;
        // 添加底部跟踪的View 到 IndicatorGroupView中
        addView(mBottomTrackView);

        // 对下标进行配置
        initBottomTrackView();
    }

    /**
     * 设置底部指示器的宽高，并正确显示它的位置
     */
    private void initBottomTrackView() {
        // 设置下标居中
        mTrackParams = (LayoutParams) mBottomTrackView.getLayoutParams();
        mTrackParams.gravity = Gravity.BOTTOM;
        // 获取用户定义的下标的宽度
        int trackWidth = mTrackParams.width;

        // 没有设置具体宽度，默认为条目宽度
        if (mTrackParams.width == ViewGroup.LayoutParams.MATCH_PARENT || mTrackParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            trackWidth = mItemWidth;
        }
        // 没有设置具体高度，默认为 5dp
        if (mTrackParams.height == ViewGroup.LayoutParams.MATCH_PARENT || mTrackParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            mTrackParams.height = 5;
        }

        // 如果设置的宽度数值大于条目宽度，重设为条目宽度
        if (trackWidth > mItemWidth) {
            trackWidth = mItemWidth;
        }

        // 最后设置下标的宽度
        mTrackParams.width = trackWidth;

        // 实现下标位于条目的最中间
        mOriginLeftMargin = (mItemWidth - trackWidth) / 2;
        mTrackParams.leftMargin = mOriginLeftMargin;
    }

    /**
     * 根据 position和 positionOffset来计算 leftMargin，进而移动下标到相应位置
     * 这种移动下标的方式适合短距离移动
     *
     * @param position       条目位置
     * @param positionOffset 位移量
     */

    public void scrollBottomTrack(int position, float positionOffset) {
        if (mBottomTrackView == null) {
            return;
        }

        int leftMargin = (int) ((position + positionOffset) * mItemWidth);
        // 控制 leftMargin 去移动下标
        mTrackParams.leftMargin = leftMargin + mOriginLeftMargin;
        mBottomTrackView.setLayoutParams(mTrackParams);
    }

    /**
     * 移动下标到 position 位置
     *
     * @param position 目的位置
     */
    public void scrollBottomTrack(int position) {
        if (mBottomTrackView == null) {
            return;
        }
        // 最终要移动的位置
        int finalLeftMargin = ((position) * mItemWidth) + mOriginLeftMargin;
        // 当前的位置
        int currentLeftMargin = mTrackParams.leftMargin;
        // 移动的距离（正负）
        int distance = finalLeftMargin - currentLeftMargin;

        // 移动过程中使用动画
        ValueAnimator animator = ObjectAnimator.ofFloat(currentLeftMargin, finalLeftMargin)
                .setDuration((long) (Math.abs(distance) * 0.4f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 会不断的回掉这个方法 不断的设置leftMargin
                float currentLeftMargin = (Float) animation.getAnimatedValue();
                mTrackParams.leftMargin = (int) currentLeftMargin;
                mBottomTrackView.setLayoutParams(mTrackParams);
            }
        });
        // 插值器：让速度先快然后越来越慢
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
}
