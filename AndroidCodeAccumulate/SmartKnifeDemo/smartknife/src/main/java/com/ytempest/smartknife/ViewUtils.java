package com.ytempest.smartknife;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * @author ytempest
 *         Description：
 */
public class ViewUtils {
    public static <T extends View> T findViewById(Object object, int viewId) {
        View view = null;
        if (object instanceof Activity) {
            view = getViewByActivity(object, viewId);
        } else if (object instanceof Fragment) {
            view = getViewByFragment(object, viewId);
        }

        return (T) view;
    }


    private static View getViewByActivity(Object object, int viewId) {
        return ((Activity) object).findViewById(viewId);
    }

    private static View getViewByFragment(Object object, int viewId) {
        View rootView = ((Fragment) object).getView();
        if (rootView != null) {
            return rootView.findViewById(viewId);
        } else {
            throw new IllegalArgumentException(object.getClass().getName() + "didn't have layout, please setting");
        }
    }
}


