package com.ytempest.framelibrary.skin.attr;

import android.view.View;

/**
 * @author ytempest
 *         Description: 每一个SkinAttr保存着一个属性名以及其引用的资源名；
 *         如src="@drawable/iv_photo" ，属性名：src，资源名：iv_photo
 */
public class SkinAttr {
    /**
     * 属性名
     */
    private SkinType mSkinType;
    /**
     * 资源名
     */
    private String mResName;

    public SkinAttr(String resName, SkinType skinType) {
        this.mResName = resName;
        this.mSkinType = skinType;
    }

    /**
     * 更换皮肤
     *
     * @param view 需要换肤的View
     */
    public void changeSkin(View view) {
        mSkinType.changeSkin(view, mResName);
    }

}
