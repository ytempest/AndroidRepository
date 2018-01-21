package com.ytempest.framelibrary.skin;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * @author ytempest
 *         Description: 皮肤资源包的管理类
 */
public class SkinResource {

    private static final String TAG = "SkinResource";
    /**
     * 资源通过这个对象获取
     */
    private Resources mSkinResource;
    private String mPackageName;

    public SkinResource(Context context, String skinPath) {
        try {
            Resources superRes = context.getResources();

            AssetManager asset = AssetManager.class.newInstance();

            // 添加本地下载好的资源皮肤，Native层 C和C++是怎么搞的
            Method method = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);

            // 反射执行方法
            method.invoke(asset, skinPath);

            mSkinResource = new Resources(asset, superRes.getDisplayMetrics(),
                    superRes.getConfiguration());

            // 获取 skinPath 皮肤包名
            mPackageName = context.getPackageManager()
                    .getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES).packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过资源名字从皮肤包中获取 Drawable
     *
     * @param resName
     * @return
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
     * @param resName
     * @return
     */
    public ColorStateList getColorByName(String resName) {
        try {
            int resId = mSkinResource.getIdentifier(resName, "color", mPackageName);
            ColorStateList color = mSkinResource.getColorStateList(resId);
            return color;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
