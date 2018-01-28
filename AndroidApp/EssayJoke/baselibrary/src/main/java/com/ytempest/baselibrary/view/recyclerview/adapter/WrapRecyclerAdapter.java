package com.ytempest.baselibrary.view.recyclerview.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author ytempest
 *         Description：使用装饰模式，对RecyclerView的Adapter扩展可以添加头部View以及底部View的功能
 */
public class WrapRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private SparseArray<View> mHeaderViews;
    private SparseArray<View> mFooterViews;
    /**
     * 头布局View集合的起点key，用于viewType识别
     */
    private static int HEADER_BASE_ITEM_TYPE = 1000000;
    /**
     * 底布局View集合的起点key，用于viewType识别
     */
    private static int FOOTER_BASE_ITEM_TYPE = 2000000;
    /**
     * 具体构建角色：对该角色进行功能的扩展
     */
    private RecyclerView.Adapter mAdapter;

    public WrapRecyclerAdapter(RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
        mHeaderViews = new SparseArray<>();
        mFooterViews = new SparseArray<>();
    }

    /**
     * 根据位置返回相应的布局类型
     *
     * @param position 当前View 的位置
     * @return 如果是头部View或底部View：返回它的的key；否则交由原Adapter自己处理
     */
    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterPosition(position)) {
            int index = position - mAdapter.getItemCount() - mHeaderViews.size();
            return mFooterViews.keyAt(index);
        }
        position = position - mHeaderViews.size();
        return mAdapter.getItemViewType(position);
    }

    private boolean isFooterPosition(int position) {
        return position >= mAdapter.getItemCount() + mHeaderViews.size();
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaderViews.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isHeaderViewType(viewType)) {
            View headerView = mHeaderViews.get(viewType);
            return createHeaderFooterViewHolder(headerView);
        } else if (isFooterViewType(viewType)) {
            View footerView = mFooterViews.get(viewType);
            return createHeaderFooterViewHolder(footerView);
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    private boolean isFooterViewType(int viewType) {
        int index = mFooterViews.indexOfKey(viewType);
        return index >= 0;
    }

    private RecyclerView.ViewHolder createHeaderFooterViewHolder(View view) {
        return new RecyclerView.ViewHolder(view) {
        };
    }

    private boolean isHeaderViewType(int viewType) {
        int index = mHeaderViews.indexOfKey(viewType);
        return index >= 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isHeaderPosition(position) || isFooterPosition(position)) {
            return;
        }
        // 计算出 原Adapter的view位置
        int adapterPosition = position - mHeaderViews.size();
        mAdapter.onBindViewHolder(holder, adapterPosition);
    }

    @Override
    public int getItemCount() {
        // RecyclerView列表的真正子View数目
        return mAdapter.getItemCount() + mHeaderViews.size() + mFooterViews.size();
    }

    /**
     * 添加头部View
     */
    public void addHeaderView(View view) {
        int index = mHeaderViews.indexOfValue(view);
        if (index < 0) {
            mHeaderViews.put(HEADER_BASE_ITEM_TYPE++, view);
        }
        notifyDataSetChanged();
    }

    /**
     * 添加底部View
     */
    public void addFooterView(View view) {
        int index = mFooterViews.indexOfValue(view);
        if (index < 0) {
            mFooterViews.put(FOOTER_BASE_ITEM_TYPE++, view);
        }
        notifyDataSetChanged();
    }

    /**
     * 移除头部View
     */
    public void removeHeader(View view) {
        int index = mHeaderViews.indexOfValue(view);
        if (index < 0) {
            return;
        }
        mHeaderViews.removeAt(index);
        notifyDataSetChanged();
    }

    /**
     * 移除底部View
     */
    public void removeFooter(View view) {
        int index = mFooterViews.indexOfValue(view);
        if (index < 0) {
            return;
        }
        mFooterViews.removeAt(index);
        notifyDataSetChanged();
    }

    /**
     * 解决设置网格布局后，添加的头布局和底布局都不占用一行的问题
     *
     * @param recyclerView 需要进行调整的RecyclerView
     */
    public void adjustSpanSize(final RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    boolean isHeaderOrFooter = isHeaderPosition(position) || isFooterPosition(position);
                    return isHeaderOrFooter ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

}
