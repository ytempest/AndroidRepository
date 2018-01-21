package com.ytempest.recycleranalysis.commonRecyclerUse;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ytempest.recycleranalysis.commonRecyclerUse.adapter.CommonViewHolder;



/**
 * @author ytempest
 *         Description：
 */

public  class GlideImageLoader extends CommonViewHolder.CommonImageLoader {

    public GlideImageLoader(String imagePath) {
        super(imagePath);
    }

    @Override
    public void displayImage(Context context, ImageView imageView, String imagePath) {
        Glide.with(context).load(imagePath).into(imageView);
    }
}
