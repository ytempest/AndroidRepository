package com.ytempest.baselibrary.view.navigation;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author ytempest
 *         Description: 构建一个 Navigation 的框架，不涉及业务功能，有关业务逻辑的功能由子类进行拓展
 */
public abstract class AbstractNavigationBar<P extends AbstractNavigationBar.Builder.AbsNavigationParams> implements INavigationBar {

    /**
     * 构建导航条的配置类
     */
    private P mParams;

    /**
     * 构建的目的导航条
     */
    private View mNavigationView;

    public AbstractNavigationBar(P params) {
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
     */
    protected void setText(int viewId, String text) {
        if (text != null) {
            TextView tv = findViewById(viewId);
            if (!TextUtils.isEmpty(text)) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(text);
            }
        }
    }

    /**
     * 设置文本
     */
    protected void setText(int viewId, int textId) {
        if (textId != -1) {
            TextView tv = findViewById(viewId);
            tv.setVisibility(View.VISIBLE);
            tv.setText(textId);
        }
    }

    /**
     * 设置点击
     */
    protected void setOnClickListener(int viewId, View.OnClickListener listener) {
        if (listener != null) {
            findViewById(viewId).setOnClickListener(listener);
        }
    }

    /**
     * 设置View隐藏
     */
    public void setVisibility(int viewId, int visible) {
        if (viewId != -1) {
            findViewById(viewId).setVisibility(visible);
        }
    }

    /**
     * 设置背景颜色
     */
    public void setBackground(@DrawableRes int resId) {
        if (resId != -1) {
            mNavigationView.setBackgroundResource(resId);
        }
    }

    /**
     * 设置文字颜色
     */
    public void setTextColor(int viewId, int colorId) {
        if (colorId != -1) {
            int color;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                color = mNavigationView.getContext().getResources().getColor(colorId, null);
            } else {
                color = mNavigationView.getContext().getResources().getColor(colorId);
            }
            ((TextView) findViewById(viewId)).setTextColor(color);
        }
    }

    /**
     * 设置icon
     */
    public void setDrawable(int viewId, int drawableId) {
        if (drawableId != -1) {
            View view = findViewById(viewId);
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(drawableId);
            }
        }
    }


    public <T extends View> T findViewById(int viewId) {
        return (T) mNavigationView.findViewById(viewId);
    }

    /**
     * 构建一个基本的 Navigation
     */
    private void createAndBindView() {

        if (mParams.mParent == null) {
            // 设置导航条在顶部的两个方法
            // 方法一：这种方法可能会导致在Android4.4版本实现沉浸式状态栏时会出现问题，故不再采用这种方式
          /*  ViewGroup activityRoot = (ViewGroup) ((Activity) (mParams.mContext))
                    .getWindow().getDecorView();
            mParams.mParent = (ViewGroup) activityRoot.getChildAt(0);*/

            // 方法二：通过给Activity的布局增加一层LinearLayout布局实现
            // 获取Activity的根布局
            ViewGroup androidContainer = (ViewGroup) ((Activity) (mParams.mContext)).findViewById(android.R.id.content);
            // 获取Activity的布局，也就是setContentView设置的布局
            ViewGroup originView = (ViewGroup) androidContainer.getChildAt(0);
            androidContainer.removeViewAt(0);
            // 给原布局加上一层LinearLayout
            LinearLayout wrapperView = wrapperContentView(originView);
            androidContainer.addView(wrapperView, 0);
            mParams.mParent = wrapperView;
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
     * 给 originView添加一层LinearLayout
     */
    private LinearLayout wrapperContentView(ViewGroup originView) {
        LinearLayout wrapperView = new LinearLayout(mParams.mContext);
        wrapperView.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.
                LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        wrapperView.addView(originView, param);
        return wrapperView;
    }


    /**
     * Builder约束了 Navigation 的构建
     */
    public abstract static class Builder {

        public Builder(Context context, ViewGroup parent) {
        }

        public abstract AbstractNavigationBar build();


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
