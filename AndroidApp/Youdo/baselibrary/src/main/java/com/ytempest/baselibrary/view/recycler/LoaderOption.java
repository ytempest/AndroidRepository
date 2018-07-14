package com.ytempest.baselibrary.view.recycler;

/**
 * @author ytempest
 *         Description：用于配置图片加载的一些额外配置，如占位符等
 */
public class LoaderOption {
    public int placeHolder;
    public int error;

    public LoaderOption() {
    }

    public LoaderOption placeHolder(int resId) {
        placeHolder = resId;
        return this;
    }

    public LoaderOption error(int resId) {
        error = resId;
        return this;
    }
}
