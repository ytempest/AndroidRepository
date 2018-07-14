package com.ytempest.framelibrary.skin.attr;

import android.view.View;

import java.util.List;

/**
 * @author ytempest
 *         Description: 保存着需要换肤的View以及它的所有需要换肤的属性，在需要换肤的时候能实现对这个View实现换肤
 */
public class SkinView {
    /**
     * 需要进行换肤的 View
     */
    private View mView;
    /**
     * 需要进行换肤的属性集合，如Button的textColor，background
     */
    private List<SkinAttr> mSkinAttrs;

    public SkinView(View view, List<SkinAttr> skinAttrs) {
        this.mView = view;
        this.mSkinAttrs = skinAttrs;
    }

    /**
     * 遍历将每一个要换肤的属性都应用到 View 中
     */
    public void changeSkin() {
        for (SkinAttr attr : mSkinAttrs) {
            attr.changeSkin(mView);
        }
    }
}
