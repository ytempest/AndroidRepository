package com.ytempest.baselibrary.view.recyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author ytempest
 *         Description：
 */
public class LoadRecyclerView extends RefreshRecyclerView {

    /**
     * 默认的上拉状态
     */
    public static int LOAD_STATUS_NORMAL = 0x000111;
    /**
     * 正在上拉的状态
     */
    public static int LOAD_STATUS_ON_PULL = 0x000222;
    /**
     * 松开上拉的状态
     */
    public static int LOAD_STATUS_LOOSE_PULL = 0x000333;
    /**
     * 正在加载的状态
     */
    public static int LOAD_STATUS_LOADING = 0x000444;

    private LoadViewCreator mLoadViewCreator;

    private View mLoadView;

    private float mFingerDownY;

    private int mLoadViewHeight = 0;
    private boolean mCurrentDrag = false;

    private int mCurrentLoadStatus = LOAD_STATUS_NORMAL;

    private OnLoadMoreListener mLoadMoreListener;

    public LoadRecyclerView(Context context) {
        super(context);
    }

    public LoadRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        addLoadView();
    }

    public void addLoadViewCreator(LoadViewCreator creator) {
        this.mLoadViewCreator = creator;
        addLoadView();
    }

    /**
     * 添加上拉加载更多的View
     */
    private void addLoadView() {
        if (getAdapter() != null && mLoadViewCreator != null) {
            View loadView = mLoadViewCreator.getLoadView(getContext(), this);
            if (loadView != null) {
                this.mLoadView = loadView;
                mLoadViewHeight = loadView.getMeasuredHeight();
                addFooterView(loadView);
                setLoadViewBottomMargin(-mLoadViewHeight);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 当手指按下屏幕，记录该位置的Y轴坐标
                mFingerDownY = ev.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                if (mCurrentDrag) {
                    restoreLoadView();
                }
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void restoreLoadView() {
        int currentBottomMargin = ((ViewGroup.MarginLayoutParams) mLoadView.getLayoutParams()).bottomMargin;
        int finalBottomMargin = 0;
        if (mCurrentLoadStatus == LOAD_STATUS_LOOSE_PULL) {
            mCurrentLoadStatus = LOAD_STATUS_LOADING;
            if (mLoadViewCreator != null) {
                mLoadViewCreator.onLoading();
            }

            if (mLoadMoreListener != null) {
                mLoadMoreListener.onLoad();
            }
        }

        int adjustDistance = currentBottomMargin - finalBottomMargin;

        ValueAnimator animator = ValueAnimator.ofFloat(currentBottomMargin, finalBottomMargin).setDuration(adjustDistance);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float bottomMargin = (float) animation.getAnimatedValue();
                setLoadViewBottomMargin((int) bottomMargin);
            }
        });
        animator.start();
        mCurrentDrag = false;
    }

    public void setLoadViewBottomMargin(int bottomMargin) {

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mLoadView.getLayoutParams();
        if (bottomMargin < mLoadViewHeight) {
            bottomMargin = mLoadViewHeight;
        }
        params.bottomMargin = bottomMargin;
        mLoadView.setLayoutParams(params);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // 如果是在最底部才处理，否则不需要处理
                if (canScrollDown() || mLoadView == null || mLoadViewCreator == null ||
                        mCurrentLoadStatus == LOAD_STATUS_LOADING) {
                    return super.onTouchEvent(e);
                }

                if (mCurrentDrag) {
                    scrollToPosition(getAdapter().getItemCount() - 1);
                }

                // 获取手指触摸拖拽的距离
                int distanceY = (int) ((e.getRawY() - mFingerDownY) * mDragIndex);

                if (distanceY < 0) {
                    if (mLoadViewCreator != null) {
                        updateLoadViewStatus(-distanceY);
                        mLoadViewCreator.onPull(-distanceY, mLoadViewHeight, mCurrentLoadStatus);
                    }
                    setLoadViewBottomMargin(-distanceY);
                    mCurrentDrag = true;
                    // 返回true代表消费该触摸事件，不再传递
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(e);
    }


    private void updateLoadViewStatus(int distanceY) {
        if (distanceY < mLoadViewHeight) {
            mCurrentLoadStatus = LOAD_STATUS_ON_PULL;
        } else {
            mCurrentLoadStatus = LOAD_STATUS_LOOSE_PULL;
        }
    }

    private boolean canScrollDown() {
        return ViewCompat.canScrollVertically(this, 1);
    }


    public void stopLoad() {
        mCurrentLoadStatus = LOAD_STATUS_NORMAL;
        restoreLoadView();
        if (mLoadViewCreator != null) {
            mLoadViewCreator.onStopLoad();
        }
    }


    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

    /**
     * Description：上拉加载更多的监听接口
     */
    public interface OnLoadMoreListener {
        void onLoad();
    }
}
