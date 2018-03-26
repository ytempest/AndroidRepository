package com.ytempest.baselibrary.view.recyclerview.division;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author ytempest
 *         Description：RecyclerView分界线类，支持使用图片作为分界线，并支持水平和垂直方向的
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] DEFAULT_ATTRS = new int[]{android.R.attr.listDivider};

    public static final int HORIZONTAL = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;

    private int mOrientation = VERTICAL;

    public DividerItemDecoration(Context context) {
        final TypedArray array = context.obtainStyledAttributes(DEFAULT_ATTRS);
        mDivider = array.getDrawable(0);
        array.recycle();
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation: " + orientation);
        }
        mOrientation = orientation;
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
     * 该方法在RecyclerView测量子View的宽高的时候会调用，作用是为分割线空出位置
     *
     * @param outRect 当前子View的Rect
     * @param view    当前测量的子View
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // 默认第一个view不留位置
        if (parent.getChildLayoutPosition(view) != 0) {
            if (mOrientation == VERTICAL) {
                outRect.set(0, mDivider.getIntrinsicHeight(), 0, 0);
            } else {
                outRect.set(mDivider.getIntrinsicWidth(), 0, 0, 0);
            }
        }
    }

    /**
     * 在绘制RecyclerView的子view之间的空余空间时会调用
     *
     * @param canvas 子View的canvas
     */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            drawVertical(canvas, parent);
        } else {
            drawHorizontal(canvas, parent);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        // 分割线左边的绘制位置
        final int left = parent.getPaddingLeft();
        // 分割线右边的绘制位置
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }


    private void drawHorizontal(Canvas canvas, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
    }
}
