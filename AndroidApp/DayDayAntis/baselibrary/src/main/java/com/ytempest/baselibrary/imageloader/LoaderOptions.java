package com.ytempest.baselibrary.imageloader;

import android.graphics.drawable.Drawable;

/**
 * @author ytempest
 *         Description：图片加载策略的配置类
 */
public class LoaderOptions {

    /**
     * 当没有成功加载的时候显示的图片
     */
    private int mPlaceHolder = -1;
    /**
     * 加载错误的时候显示的drawable
     */
    private int mErrorDrawableId = -1;
    /**
     * 加载错误的时候显示的drawable
     */
    private Drawable mErrorDrawable = null;
    /**
     * 是否渐变平滑的显示图片
     */
    private boolean mIsCrossFade = false;
    /**
     * 是否跳过内存缓存
     */
    private boolean mIsSkipMemoryCache = false;


    public LoaderOptions(Builder builder) {
        this.mPlaceHolder = builder.placeHolder;
        this.mErrorDrawableId = builder.errorDrawableId;
        this.mIsCrossFade = builder.isCrossFade;
        this.mIsSkipMemoryCache = builder.isSkipMemoryCache;
        this.mErrorDrawable = builder.errorDrawable;
    }

    public int getPlaceHolder() {
        return mPlaceHolder;
    }

    public int getErrorDrawableId() {
        return mErrorDrawableId;
    }

    public Drawable getErrorDrawable() {
        return mErrorDrawable;
    }


    public boolean isCrossFade() {
        return mIsCrossFade;
    }

    public boolean isSkipMemoryCache() {
        return mIsSkipMemoryCache;
    }


    public static class Builder {
        private int placeHolder = -1;
        private int errorDrawableId = -1;
        private Drawable errorDrawable = null;
        private boolean isCrossFade = false;
        private boolean isSkipMemoryCache = false;

        public Builder() {
        }

        public Builder placeHolder(int drawable) {
            this.placeHolder = drawable;
            return this;
        }

        public Builder errorDrawableId(int errorDrawableId) {
            this.errorDrawableId = errorDrawableId;
            return this;
        }

        public Builder errorDrawable(Drawable errorDrawable) {
            this.errorDrawable = errorDrawable;
            return this;
        }

        public Builder isCrossFade(boolean isCrossFade) {
            this.isCrossFade = isCrossFade;
            return this;
        }

        public Builder isSkipMemoryCache(boolean isSkipMemoryCache) {
            this.isSkipMemoryCache = isSkipMemoryCache;
            return this;
        }

        public LoaderOptions build() {
            return new LoaderOptions(this);
        }
    }
}
