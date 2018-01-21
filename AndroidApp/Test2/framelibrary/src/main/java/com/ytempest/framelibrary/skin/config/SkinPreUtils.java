package com.ytempest.framelibrary.skin.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author ytempest
 *         Description:
 */
public class SkinPreUtils {

    private static SkinPreUtils mInstance;
    private Context mContext;

    private SkinPreUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    public static SkinPreUtils getInstance(Context context) {
        if (mInstance == null) {
            synchronized (SkinPreUtils.class) {
                if (mInstance == null) {
                    mInstance = new SkinPreUtils(context);
                }
            }
        }
        return mInstance;
    }

    /**
     * 保存皮肤的路径
     *
     * @param skinPath
     */
    public void saveSkinPath(String skinPath) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(SkinConfig.SKIN_INFO_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(SkinConfig.SKIN_PATH_NAME, skinPath).apply();
    }

    /**
     * 获取皮肤路径
     *
     * @return
     */
    public String getSkinPath() {
        return mContext.getSharedPreferences(SkinConfig.SKIN_INFO_NAME, Context.MODE_PRIVATE)
                .getString(SkinConfig.SKIN_PATH_NAME, "");
    }


    public void clearSkinInfo() {
        saveSkinPath("");
    }
}
