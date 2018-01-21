package com.ytempest.framelibrary.skin.attr;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ytempest.framelibrary.skin.SkinManager;
import com.ytempest.framelibrary.skin.SkinResource;

/**
 * @author ytempest
 *         Description: 记录了所有要换肤的属性，Button的textColor、background，ImageView的background、src
 */
public enum SkinType {

    TEXT_COLOR("textColor") {
        @Override
        public void changeSkin(View view, String resName) {
            SkinResource skinResource = getSkinResource();
            ColorStateList color = skinResource.getColorByName(resName);
            if (color == null) {
                return;
            }

            TextView textView = (TextView) view;
            textView.setTextColor(color);
        }
    },
    BACKGROUND("background") {
        @Override
        public void changeSkin(View view, String resName) {
            // 背景可能是图片也可能是颜色
            SkinResource skinResource = getSkinResource();
            // 可能是图片
            Drawable drawable = skinResource.getDrawableByName(resName);
            if (drawable != null) {
                view.setBackground(drawable);
                return;
            }

            // 可能是颜色
            ColorStateList color = skinResource.getColorByName(resName);
            if (color != null) {
                view.setBackgroundColor(color.getDefaultColor());
            }
        }
    }, SRC("src") {
        @Override
        public void changeSkin(View view, String resName) {
            SkinResource skinResource = getSkinResource();
            Drawable drawable = skinResource.getDrawableByName(resName);
            if (drawable != null) {
                ImageView imageView = (ImageView) view;
                imageView.setImageDrawable(drawable);
            }
        }
    };

    private static final String TAG = "SkinType";
    /**
     * 会根据名字调对应的方法
     */
    private String mResName;

    SkinType(String resName) {
        this.mResName = resName;
    }

    /**
     * 更换皮肤
     *
     * @param view    需要换肤的View
     * @param resName 需要换肤的属性的资源名
     */
    public abstract void changeSkin(View view, String resName);

    public String getResName() {
        return mResName;
    }

    public SkinResource getSkinResource() {
        return SkinManager.getInstance().getSkinResource();
    }
}
