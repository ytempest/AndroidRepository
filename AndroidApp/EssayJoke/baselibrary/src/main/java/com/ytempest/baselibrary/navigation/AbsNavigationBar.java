package com.ytempest.baselibrary.navigation;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author ytempest
 *         Description: 构建一个 Navigation 的框架，不涉及业务功能，有关业务逻辑的功能由子类进行拓展
 */
public abstract class AbsNavigationBar<P extends AbsNavigationBar.Builder.AbsNavigationParams> implements INavigationBar {

    /**
     * 构建导航条的配置类
     */
    private P mParams;

    /**
     * 构建的目的导航条
     */
    private View mNavigationView;

    public AbsNavigationBar(P params) {
        this.mParams = params;
        createAndBindView();
    }

    /**
     * 获取构建导航条的配置类，以获取配置类中相应的属性
     *
     * @return 构建导航条的一个配置类
     */
    public P getParams() {
        return mParams;
    }

    /**
     * 设置文本
     *
     * @param viewId View的id
     * @param text   文本
     */
    protected void setText(int viewId, String text) {
        TextView tv = findViewById(viewId);
        if (!TextUtils.isEmpty(text)) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    /**
     * 设置点击
     */
    protected void setOnClickListener(int viewId, View.OnClickListener listener) {
        findViewById(viewId).setOnClickListener(listener);
    }

    /**
     * 设置View隐藏
     */
    public void setVisibility(int viewId, int visible) {
        findViewById(viewId).setVisibility(visible);
    }


    public <T extends View> T findViewById(int viewId) {
        return (T) mNavigationView.findViewById(viewId);
    }

    /**
     * 构建一个基本的 Navigation
     */
    private void createAndBindView() {

        if (mParams.mParent == null) {
            // 获取activity的根布局
            ViewGroup activityRoot = (ViewGroup) ((Activity) (mParams.mContext))
                    .getWindow().getDecorView();
            mParams.mParent = (ViewGroup) activityRoot.getChildAt(0);
        }

        if (mParams.mParent == null) {
            return;
        }

        // 1. 创建导航条navigation
        mNavigationView = LayoutInflater.from(mParams.mContext).
                inflate(bindLayoutId(), mParams.mParent, false);

        // 2.将navigation添加到布局中去
        mParams.mParent.addView(mNavigationView, 0);
        // 让子类添加navigation的业务功能
        applyView();
    }


    /**
     * Builder约束了 Navigation 的构建
     */
    public abstract static class Builder {

        public Builder(Context context, ViewGroup parent) {
        }

        public abstract AbsNavigationBar build();


        /**
         * 只存储一些构建 Navigation 的底层属性
         */
        public static class AbsNavigationParams {
            public Context mContext;
            /**
             * navigation的父布局
             */
            public ViewGroup mParent;

            public AbsNavigationParams(Context context, ViewGroup parent) {
                this.mContext = context;
                this.mParent = parent;
            }
        }
    }
}
