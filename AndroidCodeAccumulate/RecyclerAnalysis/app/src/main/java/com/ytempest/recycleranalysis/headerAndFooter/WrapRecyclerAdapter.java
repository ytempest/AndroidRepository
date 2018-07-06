package com.ytempest.recycleranalysis.headerAndFooter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import static android.content.ContentValues.TAG;

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
    private  int HEADER_BASE_ITEM_TYPE = 100;
    /**
     * 底布局View集合的起点key，用于viewType识别
     */
    private  int FOOTER_BASE_ITEM_TYPE = 200;
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
            // keyAt()方法获取的是View添加到SparseArray时的key
            return mHeaderViews.keyAt(position);
        } else if (isFooterPosition(position)) {
            int index = position - mAdapter.getItemCount() - mHeaderViews.size();
            return mFooterViews.keyAt(index);
        }
        position = position - mHeaderViews.size();
        return mAdapter.getItemViewType(position);
    }

    private boolean isFooterPosition(int position) {
        return position >= (mAdapter.getItemCount() + mHeaderViews.size());
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaderViews.size();
    }

    /**
     * @param viewType 这个就是在 getItemViewType()方法返回的View的类型，由于我们添加的头部和底部View
     *                 是使用 HEADER_BASE_ITEM_TYPE 和 FOOTER_BASE_ITEM_TYPE 来辨别的，所以如果是头部
     *                 或底部，那么这个 viewType就是这两个参数
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isHeaderViewType(viewType)) {
            // 根据指定的viewType（其实就是mHeaderViews的索引）获取相应的头部View
            View headerView = mHeaderViews.get(viewType);
            return createHeaderFooterViewHolder(headerView);
        }
        if (isFooterViewType(viewType)) {
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
        return mAdapter.getItemCount() + mFooterViews.size() + mHeaderViews.size();
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
