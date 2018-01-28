package com.ytempest.baselibrary.view.recyclerview.adapter;

/**
 * @author ytempest
 *         Description：CommonRecyclerView的子View的多布局支持接口
 */
public interface MultiTypeSupport<T> {
    /**
     * 根据当前RecyclerView条目的数据以及位置返回相应布局id
     *
     * @param item     当前条目数据
     * @param position 当前条目位置
     * @return 布局id
     */
     int getLayoutId(T item, int position);
}
