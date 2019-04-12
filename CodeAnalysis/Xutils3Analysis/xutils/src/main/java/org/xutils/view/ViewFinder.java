package org.xutils.view;

import android.app.Activity;
import android.view.View;

/**
 * Author: wyouflf
 * Date: 13-9-9
 * Time: 下午12:29
 */

/**
 * ytempest
 * desc: 该类是一个通过 Activity 或者 View 来搜索指定 View 的辅助类
 */
/*package*/ final class ViewFinder {

    private View view;
    private Activity activity;

    public ViewFinder(View view) {
        this.view = view;
    }

    public ViewFinder(Activity activity) {
        this.activity = activity;
    }

    public View findViewById(int id) {
        if (view != null) return view.findViewById(id);
        if (activity != null) return activity.findViewById(id);
        return null;
    }

    public View findViewByInfo(ViewInfo info) {
        return findViewById(info.value, info.parentId);
    }


    /**
     * 从父布局 id 所在的 View 中搜索目的 View，如果父布局 View 不存在，则在当前的 View 中搜索
     *
     * @param id  要搜索的目的 View 的 id
     * @param pid 父布局的 id
     */
    public View findViewById(int id, int pid) {
        View pView = null;
        if (pid > 0) {
            pView = this.findViewById(pid);
        }

        View view = null;
        if (pView != null) {
            view = pView.findViewById(id);
        } else {
            view = this.findViewById(id);
        }
        return view;
    }

    /*public Context getContext() {
        if (view != null) return view.getContext();
        if (activity != null) return activity;
        return null;
    }*/
}
