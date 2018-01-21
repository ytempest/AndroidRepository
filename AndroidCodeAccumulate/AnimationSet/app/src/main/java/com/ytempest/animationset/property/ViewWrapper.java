package com.ytempest.animationset.property;

import android.view.View;

/**
 * @author ytempest
 *         Description：
 */
public class ViewWrapper {
    private View mTarget;

    // 构造方法:传入需要包装的对象
    public ViewWrapper(View target) {
        mTarget = target;
    }

    // 为宽度设置 get() & set()
    public int getWidth() {
        return mTarget.getLayoutParams().width;
    }

    public void setWidth(int width) {
        mTarget.getLayoutParams().width = width;
        mTarget.requestLayout();
    }
}
