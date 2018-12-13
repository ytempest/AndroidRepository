package com.ytempest.baselibrary.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.ytempest.baselibrary.R;


/**
 * @author ytempest
 *         Description: 一个Dialog框架，不处理Dialog的功能，而是将处理交由AlertController处理和管理
 */
public class AlertDialog extends Dialog {

    private AlertController mAlertController;

    public AlertDialog(Context context, int themeResId) {
        super(context, themeResId);
        mAlertController = new AlertController(this, getWindow());
    }

    /**
     * 设置文本
     *
     * @param viewId 设置文本的TextView
     * @param text   文本内容
     */
    public void setText(int viewId, CharSequence text) {
        mAlertController.setText(viewId, text);
    }

    public <T extends View> T getView(int viewId) {
        return mAlertController.getView(viewId);
    }

    /**
     * 设置点击事件
     */
    public void setOnClickListener(int viewId, View.OnClickListener listener) {
        mAlertController.setOnclickListener(viewId, listener);
    }

    /**
     * 获取dialog的布局
     */
    public View getContentView() {
        return mAlertController.getContentView();
    }


    public static class Builder {

        private final AlertController.AlertParams P;

        public Builder(Context context) {
            this(context, R.style.alert_dialog);
            if (context == context.getApplicationContext()) {
                throw new IllegalArgumentException("AlertDialog 的上下文不能使用ApplicationContext！ ");
            }
        }

        public Builder(Context context, int themeResId) {
            P = new AlertController.AlertParams(context, themeResId);
        }

        /**
         * 通过 view设置dialog的布局view
         */
        public Builder setContentView(View view) {
            P.mView = view;
            P.mViewLayoutResId = 0;
            return this;
        }

        /**
         * 通过 layoutId设置dialog的布局
         */
        public Builder setContentView(int layoutId) {
            P.mView = null;
            P.mViewLayoutResId = layoutId;
            return this;
        }

        /**
         * 设置文本
         *
         * @param viewId 要设置文本的 TextView
         * @param text   文本内容
         * @return Builder
         */
        public Builder setText(int viewId, CharSequence text) {
            P.mTextArray.put(viewId, text);
            return this;
        }

        /**
         * 设置点击事件
         *
         * @param view     要设置点击事件的view
         * @param listener 点击事件
         * @return Builder
         */
        public Builder setOnClickListener(int view, View.OnClickListener listener) {
            P.mClickArray.put(view, listener);
            return this;
        }

        /**
         * 设置dialog宽度占满屏幕
         *
         * @return Builder
         */
        public Builder fullWidth() {
            P.mWidth = ViewGroup.LayoutParams.MATCH_PARENT;
            return this;
        }

        /**
         * 设置dialog从底部弹出
         *
         * @param isAnimation 是否有动画
         * @return Builder
         */
        public Builder formBottom(boolean isAnimation) {
            if (isAnimation) {
                P.mAnimations = R.style.dialog_from_bottom_anim;
            }
            P.mGravity = Gravity.BOTTOM;
            return this;
        }

        /**
         * 设置Dialog的宽高
         */
        public Builder setWidthAndHeight(int width, int height) {
            P.mWidth = width;
            P.mHeight = height;
            return this;
        }

        /**
         * 添加默认动画
         */
        public Builder addDefaultAnimation() {
            P.mAnimations = R.style.dialog_scale_anim;
            return this;
        }

        /**
         * 设置自定义动画
         *
         * @param styleAnimation 自定义动画
         */
        public Builder setAnimations(int styleAnimation) {
            P.mAnimations = styleAnimation;
            return this;
        }

        /**
         * 设置dialog触碰灰色区域以及按Back键不可关闭dialog
         */
        public Builder setCanceledOnTouchOutside(boolean cancelable) {
            P.mCanceledOnTouchOutside = cancelable;
            return this;
        }

        /**
         * 设置dialog取消时的监听器
         */
        public Builder setOnCancelListener(OnCancelListener onCancelListener) {
            P.mOnCancelListener = onCancelListener;
            return this;
        }

        /**
         * 设置dialog消失时的监听器
         */
        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            P.mOnDismissListener = onDismissListener;
            return this;
        }

        /**
         * 设置 key监听器
         */
        public Builder setOnKeyListener(OnKeyListener onKeyListener) {
            P.mOnKeyListener = onKeyListener;
            return this;
        }

        /**
         * 构建 Dialog，并将 Dialog的具体构建过程划分为两个部分，让每一个部分实现不同的 Dialog功能
         *
         * @return 具有一定功能的 Dialog
         */
        private AlertDialog create() {
            // Context has already been wrapped with the appropriate theme.
            final AlertDialog dialog = new AlertDialog(P.mContext, P.mThemeResId);
            P.apply(dialog.mAlertController);
            dialog.setCanceledOnTouchOutside(P.mCanceledOnTouchOutside);
            if (P.mCanceledOnTouchOutside) {
                dialog.setCanceledOnTouchOutside(true);
            }
            dialog.setOnCancelListener(P.mOnCancelListener);
            dialog.setOnDismissListener(P.mOnDismissListener);
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener);
            }
            return dialog;
        }

        /**
         * 显示dialog
         */
        public AlertDialog show() {
            final AlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
