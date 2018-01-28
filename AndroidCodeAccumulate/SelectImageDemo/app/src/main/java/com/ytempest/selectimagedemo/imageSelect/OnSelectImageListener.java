package com.ytempest.selectimagedemo.imageSelect;

import android.view.View;

/**
 * @author ytempest
 *         Description：图片点击的监听器
 */
public interface OnSelectImageListener {
    /**
     * 当图片被点击时会被调用
     *
     * @param view 当前点击的图片条目
     */
    void onSelect(View view);
}