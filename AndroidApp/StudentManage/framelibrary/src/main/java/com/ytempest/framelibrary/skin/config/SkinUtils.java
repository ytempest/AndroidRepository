package com.ytempest.framelibrary.skin.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author ytempest
 *         Description: 换肤资源路径的工具类，用于保存、获取、清除当前皮肤的路径
 */
public class SkinUtils {

    private static SkinUtils mInstance;
    private Context mContext;

    private SkinUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    public static SkinUtils getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SkinUtils.class) {
                if (mInstance == null) {
                    mInstance = new SkinUtils(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 保存皮肤的路径
     */
    public void saveSkinPath(String skinPath) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SkinConfig.SKIN_INFO_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(SkinConfig.SKIN_PATH_NAME, skinPath).apply();
    }

    /**
     * 获取皮肤路径
     */
    public String getSkinPath() {
        return mContext.getSharedPreferences(SkinConfig.SKIN_INFO_NAME, Context.MODE_PRIVATE)
                .getString(SkinConfig.SKIN_PATH_NAME, "");
    }


    public void clearSkinInfo() {
        saveSkinPath("");
    }
}
