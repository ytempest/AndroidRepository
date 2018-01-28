package com.ytempest.baselibrary.view.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.ytempest.baselibrary.view.recyclerview.adapter.WrapRecyclerAdapter;


/**
 * @author ytempest
 *         Description：兼容WrapRecyclerAdapter以扩展RecyclerView的功能，提供可以
 *         添加头部View、底部View、没有列表数据时显示的View、加载数据时显示的View
 */
public class WrapRecyclerView extends RecyclerView {

    /**
     * 提供了添加头部View、底部View的Adapter
     */
    private WrapRecyclerAdapter mWrapRecyclerAdapter;
    /**
     * 原RecyclerView显示数据的Adapter
     */
    private RecyclerView.Adapter mAdapter;
    /**
     * 没有列表数据显示的View
     */
    private View mEmptyView;
    /**
     * 加载数据时显示的View
     */
    private View mLoadingView;


    public WrapRecyclerView(Context context) {
        super(context);
    }

    public WrapRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WrapRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        // 防止多次设置Adapter
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(mDataObserver);
            mAdapter = null;
        }

        this.mAdapter = adapter;

        // 对原Adapter进行装饰，扩展其功能
        if (adapter instanceof WrapRecyclerAdapter) {
            mWrapRecyclerAdapter = (WrapRecyclerAdapter) adapter;
        } else {
            mWrapRecyclerAdapter = new WrapRecyclerAdapter(adapter);
        }

        // 将扩展后的 WrapRecyclerAdapter 设置为RecyclerView的Adapter
        super.setAdapter(mWrapRecyclerAdapter);

        // 为原Adapter设置观察者，监测原Adapter列表数据的变动
        mAdapter.registerAdapterDataObserver(mDataObserver);

        if (getLayoutManager() instanceof GridLayoutManager) {
            // 解决网格布局中头布View和底部View都不占用一行的问题
            mWrapRecyclerAdapter.adjustSpanSize(this);
        }

        if (mLoadingView != null && mLoadingView.getVisibility() == View.VISIBLE) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    /**
     * 重写该方法，返回原Adapter，防止因为加了头部View和底部View导致列表条目的变化
     */
    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }


    /**
     * 添加头部View
     */
    public void addHeaderView(View headerView) {
        if (mAdapter == null) {
            throw new NullPointerException("add Header view need before setAdapter() ！");
        }
        if (mWrapRecyclerAdapter != null) {
            mWrapRecyclerAdapter.addHeaderView(headerView);
        }
    }

    /**
     * 添加底部View
     */
    public void addFooterView(View footerView) {
        if (mAdapter == null) {
            throw new NullPointerException("add Footer view need before setAdapter() ！");
        }
        if (mWrapRecyclerAdapter != null) {
            mWrapRecyclerAdapter.addFooterView(footerView);
        }
    }

    public void removeHeaderView(View headerView) {
        if (mWrapRecyclerAdapter != null) {
            mWrapRecyclerAdapter.removeFooter(headerView);
        }
    }

    public void removeFooterView(View footerView) {
        if (mWrapRecyclerAdapter != null) {
            mWrapRecyclerAdapter.removeFooter(footerView);
        }
    }

    /**
     * 添加没有列表数据时显示的View
     */
    public void addEmptyView(View emptyView) {
        mEmptyView = emptyView;
        mEmptyView.setVisibility(View.GONE);
    }

    /**
     * 添加加载列表数据时显示的View
     */
    public void addLoadingView(View loadingView) {
        mLoadingView = loadingView;
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private String TAG="WrapRecyclerView";
    /**
     * Adapter数据观察者，监测列表数据的变化
     */
    private AdapterDataObserver mDataObserver = new AdapterDataObserver() {

        /**
         * 当列表的数据变化时会调用该方法
         */
        @Override
        public void onChanged() {
            if (mAdapter == null) {
                return;
            }
            if (mWrapRecyclerAdapter != mAdapter) {
                mWrapRecyclerAdapter.notifyDataSetChanged();
            }
            dataChanged();
        }

        /**
         * 当列表从positionStart位置开始到itemCount数量的列表数据变化就会调用该方法
         */
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (mAdapter == null) {
                return;
            }
            if (mWrapRecyclerAdapter != mAdapter) {
                mWrapRecyclerAdapter.notifyItemRangeChanged(positionStart, itemCount);
            }
            dataChanged();
        }

        /**
         * 当列表从positionStart位置开始到itemCount数量的列表数据变化就会调用该方法
         */
        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (mAdapter == null) {
                return;
            }
            if (mWrapRecyclerAdapter != mAdapter) {
                mWrapRecyclerAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
            }
            dataChanged();
        }

        /**
         * 当在 positionStart的位置开始插入itemCount条数据时会调用该方法
         */
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mAdapter == null) {
                return;
            }
            if (mWrapRecyclerAdapter != mAdapter) {
                mWrapRecyclerAdapter.notifyItemRangeInserted(positionStart, itemCount);
            }
            dataChanged();
        }

        /**
         * 当从 positionStart位置开始，移除itemCount条数据的时候会调用该方法
         */
        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (mAdapter == null) {
                return;
            }
            if (mWrapRecyclerAdapter != mAdapter) {
                mWrapRecyclerAdapter.notifyItemRangeRemoved(positionStart, itemCount);
            }

            dataChanged();
        }

        /**
         * 当从 fromPosition位置的数据移动到 toPosition位置的时候会调用该方法
         */
        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (mAdapter == null) {
                return;
            }
            if (mWrapRecyclerAdapter != mAdapter) {
                mWrapRecyclerAdapter.notifyItemMoved(fromPosition, toPosition);
            }
            dataChanged();
        }
    };

    /**
     * 重写该方法解决动态改变LayoutManager为GridLayoutManager时，
     * 头部View、底部View、上拉刷新View、下拉刷新View都不占用一行的问题
     */
    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (mWrapRecyclerAdapter != null && layout instanceof GridLayoutManager) {
            // 解决网格布局中头布View和底部View都不占用一行的问题
            mWrapRecyclerAdapter.adjustSpanSize(WrapRecyclerView.this);
        }

    }

    /**
     * 更新数据
     */
    private void dataChanged() {
        if (mAdapter.getItemCount() == 0) {
            if (mEmptyView == null) {
                return;
            }
            if (mEmptyView.getVisibility() == GONE) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);
            }
        }
    }

    public abstract class ItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {

        }
    }

}
