package com.ytempest.framelibrary.imageloader;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.ytempest.baselibrary.imageloader.ImageLoaderStrategy;
import com.ytempest.baselibrary.imageloader.LoaderOptions;

/**
 * @author ytempest
 *         Description：使用 Glide 加载图片的一个策略
 */
public class GlideImageLoader implements ImageLoaderStrategy {
    @Override
    public void showImage(View view, String url, LoaderOptions options) {
        if (view instanceof ImageView) {
            //将类型转换为ImageView
            ImageView imageView = (ImageView) view;
            //装配基本的参数
            DrawableTypeRequest dtr = Glide.with(imageView.getContext()).load(url);
            //装配附加参数
            loadOptions(dtr, options).into(imageView);
        }
    }

    @Override
    public void showImage(View view, int drawable, LoaderOptions options) {
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            DrawableTypeRequest dtr = Glide.with(imageView.getContext()).load(drawable);
            loadOptions(dtr, options).into(imageView);
        }
    }


    /**
     * 装载由外部设置的参数
     */
    private DrawableTypeRequest loadOptions(DrawableTypeRequest dtr, LoaderOptions options) {
        if (options == null) {
            return dtr;
        }
        if (options.getPlaceHolder() != -1) {
            dtr.placeholder(options.getPlaceHolder());
        }
        if (options.getErrorDrawableId() != -1) {
            dtr.error(options.getErrorDrawableId());
        }
        if (options.getErrorDrawable() != null) {
            dtr.error(options.getErrorDrawable());
        }
        if (options.isCrossFade()) {
            dtr.crossFade();
        }
        if (options.isSkipMemoryCache()) {
            dtr.skipMemoryCache(options.isSkipMemoryCache());
        }
        return dtr;
    }

}

