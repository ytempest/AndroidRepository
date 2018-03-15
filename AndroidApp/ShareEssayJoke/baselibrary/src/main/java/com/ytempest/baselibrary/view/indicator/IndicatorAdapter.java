package com.ytempest.baselibrary.view.indicator;

import android.view.View;
import android.view.ViewGroup;

/**
 * @author ytempest
 *         Description: 指示器的Adapter
 */
public abstract class IndicatorAdapter<T extends View> {
    /**
     * 获取总共的显示条数
     */
    public abstract int getCount();

    /**
     * 根据当前的位置获取View
     */
    public abstract T getView(int position, ViewGroup parent);

    /**
     * 高亮当前位置的状态
     *
     * @param view           当前位置的View
     * @param positionOffset 状态改变的度量，范围为 0 - 1
     */
    public void highLightIndicator(T view, float positionOffset) {

    }

    /**
     * 重置当前位置的状态
     *
     * @param view           当前位置的View
     * @param positionOffset 状态改变的度量，范围为 0 - 1
     */
    public void restoreIndicator(T view, float positionOffset) {

    }

    /**
     * 添加底部跟踪的下标
     */
    public View getBottomTrackView() {
        return null;
    }
}
