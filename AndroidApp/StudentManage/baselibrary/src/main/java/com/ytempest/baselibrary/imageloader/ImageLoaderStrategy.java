package com.ytempest.baselibrary.imageloader;

import android.view.View;


/**
 * @author ytempest
 *         Description：图片加载策略的抽象接口
 */
public interface ImageLoaderStrategy {
    void showImage(View view, String url, LoaderOptions options);

    void showImage(View view, int drawable, LoaderOptions options);
}
