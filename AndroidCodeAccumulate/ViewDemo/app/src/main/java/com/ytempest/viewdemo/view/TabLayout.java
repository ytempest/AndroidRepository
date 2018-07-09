package com.ytempest.viewdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description：一个实现流式布局的ViewGroup
 */
public class TabLayout extends ViewGroup {

    private static final String TAG = "TabLayout";
    private List<List<View>> mViewLine;

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mViewLine = new ArrayList<>();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 检测一下，因为 onMeasure()方法有可能会调用多次
        if (mViewLine != null) {
            mViewLine.clear();
            mViewLine.add(new ArrayList<View>());
        }

        // 计算父布局能提供的宽度大小
        int containerWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        // 计算父布局默认拥有的高度大小
        int parentHeight = getPaddingTop() + getPaddingBottom();
        // 当前行的宽度
        int lineWidth = 0;
        // 一行View中的最大高度
        int lineViewMaxHeight = -1;
        // 是否转到下一行
        boolean isNewLine = false;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View childView = getChildAt(i);

            // 如果父布局要获取子View的宽高，就要先调用measureChild()方法测量子View的宽高
            // 在测量子View之后才可以获取子View的宽高
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);

            // 检测View，保证该View的 marginLeft 和 marginRight值都能生效
            checkViewWidth(childView, widthMeasureSpec, heightMeasureSpec);

            MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();

            // 计算一个子View占据的真实宽度
            int childRealWidth = childView.getMeasuredWidth() + params.leftMargin + params.rightMargin;

            // 计算一个子View占据的真实高度
            int childRealHeight = childView.getMeasuredHeight() + params.topMargin + params.bottomMargin;

            // 判断当前行是否能容纳下这一个View
            if (lineWidth + childRealWidth > containerWidth) {
                List<View> viewList = new ArrayList<>();
                mViewLine.add(viewList);
                viewList.add(childView);

                // 如果当前行已经容纳不下子View，那么就更新高度，这个高度是这一行View中高度最大的
                parentHeight += lineViewMaxHeight;
                // 新一行先默认选择第一个View的高度作为lineViewMaxHeight
                lineViewMaxHeight = childRealHeight;
                lineWidth = childRealWidth;
                isNewLine = true;
            } else {
                mViewLine.get(mViewLine.size() - 1).add(childView);
                lineWidth += childRealWidth;
                lineViewMaxHeight = Math.max(lineViewMaxHeight, childRealHeight);
                isNewLine = false;
            }
        }

        if (isNewLine || mViewLine.size() == 1) {
            parentHeight += lineViewMaxHeight;
        }

        setMeasuredDimension(containerWidth, parentHeight);
    }

    /**
     * 检测View的宽度加上margin值后是否超过父布局的的宽度减去padding值，如果超过就重新测量该View
     * 传入一个新的父布局宽度去测量子View
     *
     * @param childView         要检测的子View
     * @param widthMeasureSpec  父布局的宽度测量规格
     * @param heightMeasureSpec 父布局的高度测量规格
     */
    private void checkViewWidth(View childView, int widthMeasureSpec, int heightMeasureSpec) {
        MarginLayoutParams params = (MarginLayoutParams) childView.getLayoutParams();
        final int viewWidth = childView.getMeasuredWidth() + params.leftMargin + params.rightMargin;

        // 判断该View是否超过了父布局的宽度
        if (viewWidth > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
            int mode = MeasureSpec.getMode(widthMeasureSpec);
            int height = MeasureSpec.getSize(widthMeasureSpec);

            // 计算父布局能给子View的最大宽度
            measureChild(childView,
                    MeasureSpec.makeMeasureSpec(height - params.leftMargin - params.rightMargin, mode), heightMeasureSpec);

        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int curLeft;
        int curTop = getPaddingTop();
        int lineViewMaxHeight;
        for (List<View> viewList : mViewLine) {

            curLeft = getPaddingLeft();
            lineViewMaxHeight = 0;

            for (View view : viewList) {
                MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();

                // 摆放子View的时候只需要关注摆放子View的内容部分，同时子View的padding值也不需要关注
                int curViewLeft = curLeft + params.leftMargin;
                int curViewTop = curTop + params.topMargin;
                int curViewRight = curViewLeft + view.getMeasuredWidth();
                int curViewBottom = curViewTop + view.getMeasuredHeight();

                view.layout(curViewLeft, curViewTop, curViewRight, curViewBottom);

                // 在更新父布局的宽度和高度的时候要关注子View的整个宽高（包括测量宽高和margin值）
                int curViewHeight = view.getMeasuredHeight() + +params.topMargin + params.bottomMargin;
                curLeft = curViewRight + params.rightMargin;
                // 计算当前行的View的最大高度
                lineViewMaxHeight = Math.max(lineViewMaxHeight, curViewHeight);
            }
            curTop += lineViewMaxHeight;

        }
    }

    /**
     * 记得重写这个方法，这个方法会在子View调用 getLayoutParams()获取MarginLayoutParams的时候用到
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

}
