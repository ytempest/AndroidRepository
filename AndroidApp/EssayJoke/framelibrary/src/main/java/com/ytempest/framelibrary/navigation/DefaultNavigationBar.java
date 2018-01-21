package com.ytempest.framelibrary.navigation;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.ytempest.baselibrary.navigation.AbsNavigationBar;
import com.ytempest.framelibrary.R;

/**
 * @author ytempest
 *         Description: 基于 ABSNavigationBar 框架，构建满足业务功能的 Navigation
 */
public class DefaultNavigationBar<D extends DefaultNavigationBar.Builder.DefaultNavigationParams> extends
        AbsNavigationBar<DefaultNavigationBar.Builder.DefaultNavigationParams> {

    public DefaultNavigationBar(Builder.DefaultNavigationParams params) {
        super(params);
    }

    @Override
    public int bindLayoutId() {
        return R.layout.navigation_bar;
    }

    /**
     * 实现navigation 一些与业务逻辑有关的功能
     */
    @Override
    public void applyView() {
        setText(R.id.tv_title, getParams().mTitle);
        setText(R.id.tv_right_text, getParams().mRightText);
        setOnClickListener(R.id.tv_right_text, getParams().mRightClickListener);
        setOnClickListener(R.id.ib_back, getParams().mLeftClickListener);
        setVisibility(R.id.ib_back, getParams().isLeftIconVisible);
    }


    public static class Builder extends AbsNavigationBar.Builder {

        DefaultNavigationParams P;

        public Builder(Context context, ViewGroup parent) {
            super(context, parent);
            P = new DefaultNavigationParams(context, parent);
        }

        public Builder(Context context) {
            super(context, null);
            P = new DefaultNavigationParams(context, null);
        }

        public Builder setTitle(String title) {
            P.mTitle = title;
            return this;
        }

        public Builder setRightText(String rightText) {
            P.mRightText = rightText;
            return this;
        }

        /**
         * 设置左边的点击事件
         */
        public Builder setLeftClickListener(View.OnClickListener rightListener) {
            P.mLeftClickListener = rightListener;
            return this;
        }

        /**
         * 设置右边的点击事件
         */
        public Builder setRightClickListener(View.OnClickListener rightListener) {
            P.mRightClickListener = rightListener;
            return this;
        }

        /**
         * 设置右边的图片
         */
        public Builder setRightIcon(int rightRes) {
            return this;
        }

        public Builder hideLeftIcon() {
            P.isLeftIconVisible = View.INVISIBLE;
            return this;
        }

        @Override
        public DefaultNavigationBar build() {
            return new DefaultNavigationBar(P);
        }


        public static class DefaultNavigationParams extends AbsNavigationBar.Builder.AbsNavigationParams {

            public String mTitle;
            public String mRightText;
            public View.OnClickListener mRightClickListener;
            public View.OnClickListener mLeftClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 关闭当前Activity
                    ((Activity) mContext).finish();
                }
            };
            public int isLeftIconVisible = View.VISIBLE;


            public DefaultNavigationParams(Context context, ViewGroup parent) {
                super(context, parent);
            }
        }
    }
}
