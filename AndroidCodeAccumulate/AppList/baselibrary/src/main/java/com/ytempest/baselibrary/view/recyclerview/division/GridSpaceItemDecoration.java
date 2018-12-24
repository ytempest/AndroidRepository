package com.ytempest.baselibrary.view.recyclerview.division;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * @author ytempest
 *         Description：RecyclerView网格布局的分割线，只留出分割线的空间，但是不绘制分割线
 */
public class GridSpaceItemDecoration extends RecyclerView.ItemDecoration {

    /**
     * 网格布局的列数
     */
    private int mSpanCount = 1;
    /**
     * RecyclerView的子View数目
     */
    private int mChildCount = 0;

    private int mDivisionSpace;

    public GridSpaceItemDecoration(int space) {
        this.mDivisionSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mSpanCount == 1) {
            initSpanCount(parent);
        }
        if (mChildCount == 0) {
            mChildCount = parent.getAdapter().getItemCount();
        }
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        int right = mDivisionSpace;
        int bottom = mDivisionSpace;

        if (isLastColumn(parent, itemPosition)) {
            right = 0;
        } else if (isLastRow(parent, itemPosition)) {
            bottom = 0;
        }
        outRect.set(0, 0, right, bottom);
    }


    private boolean isLastColumn(RecyclerView parent, int position) {
        LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int curOrientation = ((GridLayoutManager) layoutManager).getOrientation();
            return isItemOffset(curOrientation, GridLayoutManager.VERTICAL, position);

        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int curOrientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            return isItemOffset(curOrientation, StaggeredGridLayoutManager.VERTICAL, position);
        }
        return false;
    }

    private boolean isLastRow(RecyclerView parent, int position) {
        LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            int curOrientation = ((GridLayoutManager) layoutManager).getOrientation();
            return isItemOffset(curOrientation, GridLayoutManager.HORIZONTAL, position);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int curOrientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
            return isItemOffset(curOrientation, StaggeredGridLayoutManager.HORIZONTAL, position);
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

    /**
     * 初始化RecyclerView网格布局的列数
     */
    private void initSpanCount(View parent) {
        RecyclerView.LayoutManager layoutManager = ((RecyclerView) parent).getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            mSpanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            mSpanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
    }

}
