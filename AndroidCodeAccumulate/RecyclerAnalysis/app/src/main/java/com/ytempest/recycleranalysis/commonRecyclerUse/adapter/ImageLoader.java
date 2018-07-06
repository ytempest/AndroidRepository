package com.ytempest.recycleranalysis.commonRecyclerUse.adapter;

import android.content.Context;
import android.widget.ImageView;

/**
 * Description：图片加载器，通过实现 displayImage()方法自定义图片加载的方式
 */
public abstract class ImageLoader {
    protected String imageUrl;
    protected LoaderOption loaderOption;

    public ImageLoader(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setLoaderOption(LoaderOption option) {
        if (option != null) {
            loaderOption = option;
        }
    }

    public abstract void displayImage(Context context, ImageView imageView);
}