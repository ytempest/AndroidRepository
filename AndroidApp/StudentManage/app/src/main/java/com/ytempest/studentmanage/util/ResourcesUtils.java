package com.ytempest.studentmanage.util;

import android.content.Context;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;

/**
 * @author ytempest
 *         Description：
 */
public class ResourcesUtils {

    private static Context mContext;

    private ResourcesUtils() {
    }

    public static void init(Context context) {
        mContext = context;
    }


    public static int getInt(@IntegerRes int intRes) {
        return mContext.getResources().getInteger(intRes);
    }


    public static String getString(@StringRes int stringRes) {
        return mContext.getResources().getString(stringRes);
    }
}
