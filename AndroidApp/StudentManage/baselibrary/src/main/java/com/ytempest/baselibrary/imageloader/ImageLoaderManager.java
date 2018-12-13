package com.ytempest.baselibrary.imageloader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;


/**
 * @author ytempest
 *         Description：图片加载策略的管理类，使用的时候可以在 Application中的onCreate() 中初始化
 *         网络加载框架，或者使用的时候再指定加载图片的策略
 */
public class ImageLoaderManager implements ImageLoaderStrategy {
    private static ImageLoaderManager INSTANCE;
    private ImageLoaderStrategy mImageLoader;

    private ImageLoaderManager() {
    }

    public static ImageLoaderManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ImageLoaderManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ImageLoaderManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 在 Application中的onCreate() 中初始化网络加载框架
     */
    public void init(@NonNull ImageLoaderStrategy imageLoader) {
        mImageLoader = imageLoader;
    }


    /**
     * 设置图片加载框架
     */
    public void setImageLoader(ImageLoaderStrategy loader) {
        if (loader != null) {
            mImageLoader = loader;
        }
    }


    @Override
    public void showImage(@NonNull View view, @NonNull String url, @Nullable LoaderOptions options) {
        if (mImageLoader != null) {
            mImageLoader.showImage(view, url, options);
        }
    }


    @Override
    public void showImage(@NonNull View view, @NonNull int drawable, @Nullable LoaderOptions options) {
        if (mImageLoader != null) {
            mImageLoader.showImage(view, drawable, options);
        }
    }

}
