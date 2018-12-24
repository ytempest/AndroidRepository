package com.ytempest.baselibrary.view.recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author ytempest
 *         Description：通用ViewHolder，对ViewHolder的绑定数据和设置点击事件进行了封装
 */
public class CommonViewHolder extends RecyclerView.ViewHolder {

    /**
     * 对View进行缓存，减少findViewById的次数以优化内存
     */
    private SparseArray<View> mCacheViews;

    public CommonViewHolder(View itemView) {
        super(itemView);
        mCacheViews = new SparseArray<>();
    }


    public <T extends View> T getView(int viewId) {
        View view = mCacheViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mCacheViews.put(viewId, view);
        }
        return (T) view;
    }

    /**
     * 为指定view设置文字
     */
    public CommonViewHolder setText(int viewId, CharSequence text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    /**
     * 设置指定View是否可见
     */
    public CommonViewHolder setViewVisibility(int viewId, int visibility) {
        getView(viewId).setVisibility(visibility);
        return this;
    }

    /**
     * 为指定View设置图片
     */
    public CommonViewHolder setImageResource(int viewId, int resourceId) {
        ImageView iv = getView(viewId);
        iv.setImageResource(resourceId);
        return this;
    }

    /**
     * 通过图片路径为View设置图片
     *
     * @param loader 图片加载器，规范了加载图片的方式
     */
    public CommonViewHolder setImageByUrl(int viewId, CommonImageLoader loader) {
        if (loader == null) {
            throw new NullPointerException("CommonImageLoader is null!");
        }
        ImageView imageView = getView(viewId);
        // 显示图片
        loader.displayImage(imageView.getContext(), imageView, loader.getImageUrl());
        return this;
    }

    /**
     * RecyclerView的条目里面的view设置点击事件
     */
    public void setOnViewClickListener(int viewId, View.OnClickListener listener) {
        getView(viewId).setOnClickListener(listener);
    }

    /**
     * 为RecyclerView的条目设置点击事件
     */
    public void setOnItemClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    /**
     * 为RecyclerView的条目设置长按点击事件
     */
    public void setOnItemLongClickListener(View.OnLongClickListener listener) {
        itemView.setOnLongClickListener(listener);
    }

    /**
     * Description：图片加载器，通过实现 displayImage方法自定义图片加载的方式
     */
    public abstract static class CommonImageLoader {
        private String mImagePath;

        public CommonImageLoader(String imagePath) {
            this.mImagePath = imagePath;
        }

        public String getImageUrl() {
            return mImagePath;
        }

        public abstract void displayImage(Context context, ImageView imageView, String imagePath);
    }

}
