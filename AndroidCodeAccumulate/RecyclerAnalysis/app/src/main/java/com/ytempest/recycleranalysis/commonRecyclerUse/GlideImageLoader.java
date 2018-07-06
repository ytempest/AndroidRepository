package com.ytempest.recycleranalysis.commonRecyclerUse;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.ImageLoader;


/**
 * @author ytempest
 *         Description：
 */
public class GlideImageLoader extends ImageLoader {

    public GlideImageLoader(String imageUrl) {
        super(imageUrl);
    }

    @Override
    public void displayImage(Context context, ImageView imageView) {
        RequestBuilder<Drawable> load = Glide.with(context).load(imageUrl);

        if (loaderOption != null) {
            RequestOptions requestOptions = new RequestOptions().
                    placeholder(loaderOption.placeHolder)
                    .error(loaderOption.error);
            load.apply(requestOptions);
        }

        load.into(imageView);

    }
}
