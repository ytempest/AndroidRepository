package com.ytempest.framelibrary.view.navigation;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ytempest.baselibrary.view.navigation.AbsNavigationBar;
import com.ytempest.framelibrary.R;

import java.io.Serializable;

import static android.content.ContentValues.TAG;

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
        setTextByStatus(R.id.tv_title, getParams().mTitle, getParams().mTitleId);
        setTextByStatus(R.id.tv_right_text, getParams().mRightText, getParams().mRightTextId);
        setOnClickListener(R.id.tv_right_text, getParams().mRightClickListener);
        setOnClickListener(R.id.ib_back, getParams().mLeftClickListener);
        setVisibility(R.id.ib_back, getParams().isLeftIconVisible);
        setBackground(getParams().mBackgroundResId);
        setTextColor(R.id.tv_title, getParams().mTitleColor);
        setTextColor(R.id.tv_right_text, getParams().mRightTextColor);
        setDrawable(R.id.ib_back, getParams().mLeftIconId);
    }

    private void setTextByStatus(int viewId, String text, int textId) {
        if (text != null) {
            setText(viewId, text);
        } else if (textId != 0) {
            setText(viewId, textId);
        }
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

        public Builder setTitle(int titleId) {
            P.mTitleId = titleId;
            return this;
        }

        public Builder setRightText(String rightText) {
            P.mRightText = rightText;
            return this;
        }

        public Builder setRightText(int rightTextId) {
            P.mRightTextId = rightTextId;
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
        public Builder setLeftIcon(int leftRes) {
            P.mLeftIconId = leftRes;
            return this;
        }

        /**
         * 设置右边的图片
         */
        public Builder setRightIcon(int rightRes) {
            return this;
        }

        public Builder hideLeftIcon() {
            P.isLeftIconVisible = View.GONE;
            return this;
        }

        /**
         * 设置背景颜色
         */
        public Builder setBackground(@DrawableRes int resId) {
            P.mBackgroundResId = resId;
            return this;
        }

        public Builder setTitleColor(@DrawableRes int colorId) {
            P.mTitleColor = colorId;
            return this;
        }

        public Builder setRightTextColor(@DrawableRes int colorId) {
            P.mRightTextColor = colorId;
            return this;
        }

        @Override
        public DefaultNavigationBar build() {
            return new DefaultNavigationBar(P);
        }


        public static class DefaultNavigationParams extends AbsNavigationParams {

            public String mTitle;
            public int mTitleId;
            public int mLeftIconId = R.drawable.navigation_back_normal;
            public String mRightText;
            public int mRightTextId;
            public View.OnClickListener mRightClickListener;
            public View.OnClickListener mLeftClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 关闭当前Activity
                    ((Activity) mContext).finish();
                }
            };
            public int isLeftIconVisible = View.VISIBLE;
            public int mBackgroundResId = R.color.navigation_bar_bg;
            public int mTitleColor = R.color.navigation_title_color;
            public int mRightTextColor = R.color.navigation_right_color;


            public DefaultNavigationParams(Context context, ViewGroup parent) {
                super(context, parent);
            }
        }
    }
}
