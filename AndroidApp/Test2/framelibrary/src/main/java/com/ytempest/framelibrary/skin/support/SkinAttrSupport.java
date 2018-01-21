package com.ytempest.framelibrary.skin.support;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.ytempest.framelibrary.skin.attr.SkinAttr;
import com.ytempest.framelibrary.skin.attr.SkinType;
import com.ytempest.framelibrary.skin.attr.SkinView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ytempest
 *         Description:
 */
public class SkinAttrSupport {

    private static final String TAG = "SkinAttrSupport";

    /**
     * 从 attrs 获取换肤需要的属性
     *
     * @param context 需要换肤View的 context
     * @param attrs   需要换肤View的属性集
     * @return 一个View换肤需要的所有属性集合
     */
    public static List<SkinAttr> getSkinAttrs(Context context, AttributeSet attrs) {
        List<SkinAttr> skinAttrs = new ArrayList<>();
        // 1. View的所有属性个数
        int attrLength = attrs.getAttributeCount();
        for (int index = 0; index < attrLength; index++) {
            // 2. 获取所有的属性名称（如：src），获取属性值（如：@2130837610）
            String attrName = attrs.getAttributeName(index);
            String attrValue = attrs.getAttributeValue(index);

            // 3. 根据换肤需要的属性名获取相应的 SkinType
            SkinType skinType = getSkinType(attrName);

            if (skinType != null) {
                // 4. 根据换肤需要的属性值获取该属性的资源名称，资源的属性值都是以@符号开头的，如@2130837610
                String resName = getResName(context, attrValue);
                if (TextUtils.isEmpty(resName)) {
                    continue;
                }
                SkinAttr skinAttr = new SkinAttr(resName, skinType);
                skinAttrs.add(skinAttr);
            }

        }
        return skinAttrs;
    }


    /**
     * 根据属性值获取该属性资源的名称
     *
     * @param context   View 的上下文
     * @param attrValue 属性的值
     * @return 属性值对应的名称
     */
    private static String getResName(Context context, String attrValue) {
        if (attrValue.startsWith("@")) {
            attrValue = attrValue.substring(1);
            // 将字符串转换成int
            int resId = Integer.parseInt(attrValue);
            // 获取 resId 对应的名称（如 src 引用图片的图片名）
            return context.getResources().getResourceEntryName(resId);
        }
        return null;
    }

    /**
     * 根据换肤需要的属性名获取相应的 SkinType
     *
     * @param attrName 属性名
     * @return 属性名对应的 SkinType
     */
    private static SkinType getSkinType(String attrName) {

        SkinType[] skinTypes = SkinType.values();
        for (SkinType skinType : skinTypes) {
            if (skinType.getResName().equals(attrName)) {
                return skinType;
            }
        }
        return null;
    }
}
