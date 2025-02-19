package com.ytempest.baselibrary.view.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author ytempest
 *         Description：
 */
public interface LoadViewCreator {
    /**
     * 获取上拉加载更多的View
     *
     * @param context 上下文
     * @param parent  RecyclerView
     */
    View getLoadView(Context context, ViewGroup parent);

    /**
     * 正在上拉
     *
     * @param currentDragHeight 当前拖动的高度
     * @param loadViewHeight    总的加载高度
     * @param currentLoadStatus 当前状态
     */
    void onPull(int currentDragHeight, int loadViewHeight, int currentLoadStatus);

    /**
     * 正在加载中
     */
    void onLoading();

    /**
     * 停止加载
     */
    void onStopLoad();
}
