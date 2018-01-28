package com.ytempest.baselibrary.view.recyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author ytempest
 *         Description：下拉刷新的RecyclerView
 */
public class RefreshRecyclerView extends WrapRecyclerView {

    /**
     * 默认状态
     */
    public static int REFRESH_STATUS_NORMAL = 0x0011;
    /**
     * 正在向下拉的状态
     */
    public static int REFRESH_STATUS_ON_PULL = 0x0022;
    /**
     * 松开拖拽的状态
     */
    public static int REFRESH_STATUS_LOOSE_PULL = 0x0033;
    /**
     * 正在刷新的状态
     */
    public static int REFRESH_STATUS_REFRESHING = 0x0044;

    /**
     * 上拉刷新View的构造器
     */
    private RefreshViewCreator mRefreshViewCreator;

    /**
     * 上拉刷新View
     */
    private View mRefreshView;
    /**
     * 上拉刷新View的高度
     */
    private int mRefreshViewHeight = 0;
    /**
     * 记录手指首次按下屏幕的位置
     */
    private float mFingerDown;
    /**
     * 手指阻力系数
     */
    protected float mDragIndex = 0.35f;
    /**
     * 上拉刷新的状态
     */
    private int mCurrentRefreshStatus = REFRESH_STATUS_NORMAL;
    /**
     * 标志当前是否在拖动
     */
    private boolean mCurrentDrag = false;
    /**
     * 隐藏上拉刷新的topMargin
     */
    private int mHideTopMargin;
    /**
     * 正在刷新的监听器
     */
    private OnRefreshMoreListener mRefreshMoreListener;


    public RefreshRecyclerView(Context context) {
        super(context);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        addRefreshView();
    }

    public void addRefreshViewCreator(RefreshViewCreator creator) {
        this.mRefreshViewCreator = creator;
        addRefreshView();
    }

    private void addRefreshView() {
        // 如果Adapter为空就不能使用addHeaderView()方法
        if (getAdapter() != null && mRefreshViewCreator != null) {
            View refreshView = mRefreshViewCreator.getRefreshView(getContext(), this);
            if (refreshView != null) {
                this.mRefreshView = refreshView;
                addHeaderView(mRefreshView);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的位置，之所以写在dispatchTouchEvent那是因为如果我们处理了条目点击事件，
                // 那么就不会进入onTouchEvent里面，所以只能在这里获取
                mFingerDown = ev.getRawY();
                break;

            // 如果手指离开了屏幕就会走这个逻辑
            case MotionEvent.ACTION_UP:
                // 如果有向下拉出刷新View
                if (mCurrentDrag) {
                    restoreRefreshView();
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据当前刷新状态调整当前刷新View的位置
     * 如果当前状态是松开刷新状态，就将View的正常显示；否则，就将上拉刷新的View隐藏
     */
    private void restoreRefreshView() {
        final int currentTopMargin = ((ViewGroup.MarginLayoutParams) mRefreshView.getLayoutParams()).topMargin;
        int finalTopMargin = mHideTopMargin;
        // 如果是松开刷新状态就将最终的topMargin设置为0，让View显示在RecyclerView顶部
        if (mCurrentRefreshStatus == REFRESH_STATUS_LOOSE_PULL) {
            finalTopMargin = 0;
            mCurrentRefreshStatus = REFRESH_STATUS_REFRESHING;
            if (mRefreshViewCreator != null) {
                mRefreshViewCreator.onRefreshing();
            }
            if (mRefreshMoreListener != null) {
                mRefreshMoreListener.onRefresh();
            }
        }
        // 要进行调整的距离
        int adjustDistance = currentTopMargin - finalTopMargin;

        // 位移到指定的位置：0 或者 mHideTopMargin
        ValueAnimator animator = ValueAnimator.ofFloat(currentTopMargin, finalTopMargin).setDuration(adjustDistance);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float currentTopMargin = (float) animation.getAnimatedValue();
                // 不断设置topMargin以实现动画效果
                setRefreshViewTopMargin((int) currentTopMargin);
            }
        });
        animator.start();
        // 设置不再拖拽
        mCurrentDrag = false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // 如果是在最顶部才处理，否则不需要处理
                if (canScrollUp() || mRefreshView == null || mRefreshViewCreator == null ||
                        mCurrentRefreshStatus == REFRESH_STATUS_REFRESHING) {
                    return super.onTouchEvent(e);
                }

                // 解决下拉刷新自动滚动问题
                if (mCurrentDrag) {
                    scrollToPosition(0);
                }

                // 获取手指触摸拖拽的距离，如果向下拖拽distanceY是正数，向上则是负数
                int distanceY = (int) ((e.getRawY() - mFingerDown) * mDragIndex);

                // 更新刷新状态
                updateRefreshStatus(distanceY);

                if (mCurrentRefreshStatus != REFRESH_STATUS_NORMAL) {
                    if (mRefreshViewCreator != null) {
                        mRefreshViewCreator.onPull(distanceY, mRefreshViewHeight, mCurrentRefreshStatus);
                    }
                    int topMargin = distanceY - mRefreshViewHeight;
                    setRefreshViewTopMargin(topMargin);
                    mCurrentDrag = true;
                    // 返回false代表不将触摸事件传递到下一层，而是返回父View
                    return false;
                }
                break;

            default:
                break;
        }

        return super.onTouchEvent(e);
    }

    /**
     * 判断是不是滚动到了最顶部，这个是从SwipeRefreshLayout里面copy过来的源代码
     */
    private boolean canScrollUp() {
        if (Build.VERSION.SDK_INT < 14) {
            return ViewCompat.canScrollVertically(this, -1) || this.getScaleY() > 0;
        } else {
            return ViewCompat.canScrollVertically(this, -1);
        }
    }

    /**
     * 更新刷新状态
     */
    private void updateRefreshStatus(int distanceY) {
        if (distanceY <= 0) {
            mCurrentRefreshStatus = REFRESH_STATUS_NORMAL;
        } else if (distanceY < mRefreshViewHeight) {
            mCurrentRefreshStatus = REFRESH_STATUS_ON_PULL;
        } else {
            mCurrentRefreshStatus = REFRESH_STATUS_LOOSE_PULL;
        }
    }

    /**
     * 在 RecyclerView分配子View的位置的时候会调用该方法
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        // mRefreshViewHeight等于0表示还没有对上拉刷新View初始化
        if (mRefreshView != null && mRefreshViewHeight <= 0) {
            mRefreshViewHeight = mRefreshView.getMeasuredHeight();
            if (mRefreshViewHeight > 0) {
                // 设置能将上拉刷新View隐藏的 topMargin
                // 多留出1px防止无法判断是不是滚动到头部问题
                mHideTopMargin = -mRefreshViewHeight + 1;
                setRefreshViewTopMargin(mHideTopMargin);
            }
        }
    }

    /**
     * 设置上拉刷新View的topMargin
     */
    private void setRefreshViewTopMargin(int topMargin) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mRefreshView.getLayoutParams();
        // 如果先向下拖拽然后再向上拖拽，那么marginTop的数值有可能就比原mRefreshView的topMargin要小
        // 如果marginTop的数值比View的高度要大，就设置为View的高度
        if (topMargin < mHideTopMargin) {
            topMargin = mHideTopMargin;
        }
        params.topMargin = topMargin;
        mRefreshView.setLayoutParams(params);
    }


    public void stopRefresh() {
        mCurrentRefreshStatus = REFRESH_STATUS_NORMAL;
        restoreRefreshView();
        if (mRefreshViewCreator != null) {
            mRefreshViewCreator.onStopRefresh();
        }
    }


    public void setOnRefreshMoreListener(OnRefreshMoreListener listener) {
        this.mRefreshMoreListener = listener;
    }

    /**
     * Description：下拉刷新的监听接口
     */
    public interface OnRefreshMoreListener {
        void onRefresh();
    }
}
