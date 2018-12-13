package com.ytempest.baselibrary.view.dialog;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * @author ytempest
 *         Description: 负责管理Dialog的布局View，同时处理Dialog的内部view的功能,
 */
class DialogViewHelper {
    /**
     * Dialog组件
     */
    private View mContentView = null;
    /**
     * 防止内存泄漏
     */
    private SparseArray<WeakReference<View>> mViews;

    public DialogViewHelper(Context context, int layoutResId) {
        this();
        // 使用context.getApplicationContext()上下文加载布局，以免使用context加载一些自定义布局的时候出错
        mContentView = LayoutInflater.from(context.getApplicationContext()).inflate(layoutResId, null);
    }

    public DialogViewHelper() {
        mViews = new SparseArray<>();
    }

    /**
     * 设置布局View
     */
    public void setContentView(View contentView) {
        this.mContentView = contentView;
    }

    /**
     * 获取ContentView
     */
    public View getContentView() {
        return mContentView;
    }

    public <T extends View> T getView(int viewId) {
        WeakReference<View> viewReference = mViews.get(viewId);
        View view = null;
        if (viewReference != null) {
            view = viewReference.get();
        }
        if (view == null) {
            view = mContentView.findViewById(viewId);
            if (view != null) {
                mViews.put(viewId, new WeakReference<>(view));
            }
        }
        return (T) view;
    }

    /**
     * 设置文本
     */
    public void setText(int viewId, CharSequence text) {
        // 每次都 findViewById   减少findViewById的次数
        TextView tv = getView(viewId);
        if (tv != null) {
            tv.setText(text);
        }
    }

    /**
     * 设置点击事件
     */
    public void setOnclickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }
}