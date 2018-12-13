package com.ytempest.framelibrary.view.navigation;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.ViewGroup;

import com.ytempest.baselibrary.view.navigation.AbstractNavigationBar;
import com.ytempest.framelibrary.R;

/**
 * @author ytempest
 *         Description: 基于 AbstractNavigationBar 框架，构建满足业务功能的 Navigation
 */
public class DefaultNavigationBar<D extends DefaultNavigationBar.Builder.DefaultNavigationParams> extends
        AbstractNavigationBar<DefaultNavigationBar.Builder.DefaultNavigationParams> {

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
        Builder.DefaultNavigationParams params = getParams();
        setTextByStatus(R.id.tv_center_view, params.mTitle, params.mTitleId);
        setTextByStatus(R.id.tv_left_text, params.mLeftText, params.mLeftTextId);
        setTextByStatus(R.id.tv_right_text, params.mRightText, params.mRightTextId);

        setOnClickListener(R.id.iv_left_view, params.mLeftIconClickListener);
        setOnClickListener(R.id.tv_left_text, params.mLeftTextClickListener);
        setOnClickListener(R.id.iv_right_view, params.mRightIconClickListener);
        setOnClickListener(R.id.tv_right_text, params.mRightTextClickListener);

        setVisibility(R.id.iv_left_view, params.isLeftIconVisible);
        setVisibility(R.id.tv_left_text, params.isLeftTextVisible);
        setVisibility(R.id.iv_right_view, params.isRightIconVisible);
        setVisibility(R.id.tv_right_text, params.isRightTextVisible);

        setBackground(params.mBackgroundResId);
        setTextColor(R.id.tv_center_view, params.mTitleColorId);
        setTextColor(R.id.tv_left_text, params.mLeftTextColorId);
        setTextColor(R.id.tv_right_text, params.mRightTextColorId);
        setDrawable(R.id.iv_left_view, params.mLeftIconId);
        setDrawable(R.id.iv_right_view, params.mRightIconId);
    }

    private void setTextByStatus(int viewId, String text, int textId) {
        if (text != null) {
            setText(viewId, text);
        } else if (textId != 0) {
            setText(viewId, textId);
        }
    }


    public static class Builder extends AbstractNavigationBar.Builder {

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

        public Builder hideLeftIcon() {
            P.isLeftIconVisible = View.GONE;
            return this;
        }

        public Builder setLeftIcon(@DrawableRes int leftIconRes) {
            P.mLeftIconId = leftIconRes;
            P.isLeftIconVisible = View.VISIBLE;
            return this;
        }

        public Builder setRightIcon(@DrawableRes int rightIconRes) {
            P.mRightIconId = rightIconRes;
            P.isRightIconVisible = View.VISIBLE;
            return this;
        }

        public Builder setLeftText(String leftText) {
            P.mLeftText = leftText;
            P.isLeftTextVisible = View.VISIBLE;
            P.isLeftIconVisible = View.GONE;
            return this;
        }

        public Builder setLeftText(int leftTextId) {
            P.mLeftTextId = leftTextId;
            P.isLeftTextVisible = View.VISIBLE;
            P.isLeftIconVisible = View.GONE;
            return this;
        }

        public Builder setRightText(String rightText) {
            P.mRightText = rightText;
            P.isRightTextVisible = View.VISIBLE;
            return this;
        }

        public Builder setRightText(int rightTextId) {
            P.mRightTextId = rightTextId;
            P.isRightTextVisible = View.VISIBLE;
            return this;
        }

        public Builder setLeftIconClickListener(View.OnClickListener leftIconClickListener) {
            P.mLeftIconClickListener = leftIconClickListener;
            return this;
        }

        public Builder setLeftTextClickListener(View.OnClickListener leftTextClickListener) {
            P.mLeftTextClickListener = leftTextClickListener;
            return this;
        }

        public Builder setRightIconClickListener(View.OnClickListener rightIcontListener) {
            P.mRightIconClickListener = rightIcontListener;
            return this;
        }

        public Builder setRightTextClickListener(View.OnClickListener rightTextListener) {
            P.mRightTextClickListener = rightTextListener;
            return this;
        }

        public Builder setBackground(@DrawableRes int resId) {
            P.mBackgroundResId = resId;
            return this;
        }

        public Builder setTitleColor(@ColorRes int colorId) {
            P.mTitleColorId = colorId;
            return this;
        }

        public Builder setLeftTextColor(@ColorRes int colorId) {
            P.mLeftTextColorId = colorId;
            return this;
        }

        public Builder setRightTextColor(@ColorRes int colorId) {
            P.mRightTextColorId = colorId;
            return this;
        }

        @Override
        public DefaultNavigationBar build() {
            return new DefaultNavigationBar(P);
        }

        static class DefaultNavigationParams extends AbsNavigationParams {
            String mTitle;
            int mTitleId = -1;

            String mLeftText;
            int mLeftTextId = -1;
            String mRightText;
            int mRightTextId = -1;

            int mLeftIconId = R.drawable.icon_navigation_bar_left_icon;
            int mRightIconId = -1;

            int isLeftIconVisible = View.VISIBLE;
            int isLeftTextVisible = -1;
            int isRightIconVisible = -1;
            int isRightTextVisible = -1;

            int mBackgroundResId = -1;
            int mTitleColorId = -1;
            int mLeftTextColorId = -1;
            int mRightTextColorId = -1;

            View.OnClickListener mLeftIconClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 关闭当前Activity
                    ((Activity) mContext).finish();
                }
            };
            View.OnClickListener mLeftTextClickListener;
            View.OnClickListener mRightTextClickListener;
            View.OnClickListener mRightIconClickListener;


            DefaultNavigationParams(Context context, ViewGroup parent) {
                super(context, parent);
            }
        }
    }
}
