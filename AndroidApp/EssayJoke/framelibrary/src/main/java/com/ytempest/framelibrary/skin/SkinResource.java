package com.ytempest.framelibrary.skin;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ytempest
 *         Description: 皮肤资源包的管理类
 */
public class SkinResource {

    /**
     * 皮肤包的资源通过这个对象获取
     */
    private Resources mSkinResource;
    private final String mPackageName;

    SkinResource(Context context, String skinPath) {

        // 获取 skinPath 皮肤包名
        mPackageName = context.getPackageManager()
                .getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES).packageName;

        // 获取皮肤包的 Resource对象
        try {
            Resources superRes = context.getResources();

            AssetManager asset = AssetManager.class.newInstance();

            // 添加本地下载好的资源皮肤
            Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);

            // 反射执行方法
            method.invoke(asset, skinPath);

            mSkinResource = new Resources(asset, superRes.getDisplayMetrics(),
                    superRes.getConfiguration());


        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过资源名字从皮肤包中获取 Drawable
     *
     * @param resName 资源的名称，如 ic_launcher
     */
    public Drawable getDrawableByName(String resName) {
        try {
            // 根据资源名从皮肤包中获取资源id
            int resId = mSkinResource.getIdentifier(resName, "drawable", mPackageName);
            Drawable drawable = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                drawable = mSkinResource.getDrawable(resId, null);
            } else {
                drawable = mSkinResource.getDrawable(resId);
            }
            return drawable;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过资源名字从皮肤包中获取颜色
     *
     * @param resName 颜色资源的名称，如：main_bg
     */
    public ColorStateList getColorByName(String resName) {
        try {
            int resId = mSkinResource.getIdentifier(resName, "color", mPackageName);
            ColorStateList color = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                color = mSkinResource.getColorStateList(resId, null);
            } else {
                color = mSkinResource.getColorStateList(resId);
            }
            return color;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
