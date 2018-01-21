package com.ytempest.framelibrary.skin.attr;

import android.view.View;

/**
 * @author ytempest
 *         Description: 需要换肤的每一个属性
 */
public class SkinAttr {
    /**
     * 资源名，如：src="@drawable/iv_photo" 中的 iv_photo
     */
    private String mResName;
    /**
     * 属性名，如：src
     */
    private SkinType mSkinType;

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
