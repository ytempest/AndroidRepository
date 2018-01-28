package com.ytempest.baselibrary.view.recyclerview.division;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author ytempest
 *         Description：RecyclerView网格布局的分割线类
 */
public class GridItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * 默认分割线样式
     */
    private static int[] DEFAULT_ATTRS = {android.R.attr.listDivider};
    /**
     * 分割线图片
     */
    private Drawable mDivider;
    /**
     * 网格布局的列数
     */
    private int mSpanCount = 1;

    private int mChildCount = 0;


    public GridItemDecoration(Context context) {
        TypedArray array = context.obtainStyledAttributes(DEFAULT_ATTRS);
        mDivider = array.getDrawable(0);
        array.recycle();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mSpanCount == 1) {
            initSpanCount(parent);
        }
        if (mChildCount == 0) {
            mChildCount = parent.getAdapter().getItemCount();
        }
        int right = mDivider.getIntrinsicWidth();
        int bottom = mDivider.getIntrinsicHeight();

        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        if (isLastColumn(position, parent)) {
            right = 0;
        }
        if (isLastRow(position, parent)) {
            bottom = 0;
        }
        outRect.set(0, 0, right, bottom);
    }

    /**
     * 判断 position 是否在最后一列
     */
    private boolean isLastColumn(int position, View parent) {
        RecyclerView.LayoutManager layoutManager = ((RecyclerView) parent).getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int curOrientation = ((GridLayoutManager) layoutManager).getOrientation();
            return isItemOffset(curOrientation, GridLayoutManager.VERTICAL, position);
        }
        return false;

    }

    /**
     * 判断 position 是否在最后一行
     */
    private boolean isLastRow(int position, View parent) {
        RecyclerView.LayoutManager layoutManager = ((RecyclerView) parent).getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int curOrientation = ((GridLayoutManager) layoutManager).getOrientation();
            return isItemOffset(curOrientation, GridLayoutManager.HORIZONTAL, position);
        }
        return false;
    }

    /**
     * 根据curOrientation是否为targetOrientation，从而判断是否为最
     * 后一行和最后一列的子View的右边和底部留出分割线的位置
     *
     * @param curOrientation    当前RecyclerView的布局方向
     * @param targetOrientation 需要判断的 布局方向（Vertical、Horizontal）
     * @param position          子View的位置
     */
    private boolean isItemOffset(int curOrientation, int targetOrientation, int position) {
        if (curOrientation == targetOrientation) {
            if ((position + 1) % mSpanCount == 0) {
                return true;
            }
        } else {
            int childCount = mChildCount - mChildCount % mSpanCount - 1;
            if (position > childCount) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            drawVertical(canvas, child);
            drawHorizontal(canvas, child);
        }
    }

    /**
     * 绘制水平分割线
     *
     * @param child RecyclerView绘制的当前的子View
     */
    private void drawVertical(Canvas canvas, View child) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int left = child.getLeft() - params.leftMargin;
        int right = child.getRight() + params.rightMargin + mDivider.getIntrinsicWidth();
        int top = child.getBottom() + params.bottomMargin;
        int bottom = top + mDivider.getIntrinsicHeight();
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(canvas);
    }

    /**
     * 绘制垂直方向的分割线
     */
    private void drawHorizontal(Canvas canvas, View child) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int left = child.getRight() + params.rightMargin;
        int right = left + mDivider.getIntrinsicWidth();
        int top = child.getTop() - params.topMargin;
        int bottom = child.getBottom() + params.bottomMargin;
        mDivider.setBounds(left, top, right, bottom);
        mDivider.draw(canvas);
    }

    /**
     * 设置分割线图片
     */
    public void setDrawable(@NonNull Drawable drawable) {
        if (drawable == null) {
            throw new IllegalArgumentException("Drawable cannot be null.");
        }
        mDivider = drawable;
    }

    /**
     * 初始化RecyclerView网格布局的列数
     */
    private void initSpanCount(View parent) {
        RecyclerView.LayoutManager layoutManager = ((RecyclerView) parent).getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            mSpanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        }
    }
}
