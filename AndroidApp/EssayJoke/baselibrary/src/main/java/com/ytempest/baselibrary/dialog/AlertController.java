package com.ytempest.baselibrary.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author ytempest
 *         Description: 处理与Dialog框架有关的功能，而将Dialog的内部View 的功能的实现交由DialogViewHelper实现
 */
class AlertController {

    /**
     * 保存AlertDialog对象，管理Dialog框架的功能
     */
    private AlertDialog mDialog;
    /**
     * 保存Dialog内部的View的处理类，管理内部View
     */
    private DialogViewHelper mViewHelper;
    private Window mWindow;

    public AlertController(AlertDialog dialog, Window window) {
        this.mDialog = dialog;
        this.mWindow = window;
    }

    public void setViewHelper(DialogViewHelper viewHelper) {
        this.mViewHelper = viewHelper;
    }

    /**
     * 设置文本
     *
     * @param viewId
     * @param text
     */
    public void setText(int viewId, CharSequence text) {
        mViewHelper.setText(viewId, text);
    }

    public <T extends View> T getView(int viewId) {
        return mViewHelper.getView(viewId);
    }

    /**
     * 设置点击事件
     *
     * @param viewId
     * @param listener
     */
    public void setOnclickListener(int viewId, View.OnClickListener listener) {
        mViewHelper.setOnclickListener(viewId, listener);
    }

    /**
     * 获取Dialog
     *
     * @return 一个构建中的dialog
     */
    public AlertDialog getDialog() {
        return mDialog;
    }

    /**
     * 获取Dialog的Window
     *
     * @return 构建中dialog 的Window
     */
    public Window getWindow() {
        return mWindow;
    }

    public static class AlertParams {
        public Context mContext;
        public int mThemeResId = 0;
        /**
         * 点击空白是否能够取消  默认点击阴影可以取消
         */
        public boolean mCancelable = true;
        /**
         * 布局View
         */
        public View mView;
        /**
         * 布局layout id
         */
        public int mViewLayoutResId;
        /**
         * 动画
         */
        public int mAnimations = 0;
        /**
         * 位置
         */
        public int mGravity = Gravity.CENTER;
        /**
         * 宽度
         */
        public int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        /**
         * 高度
         */
        public int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        /**
         * 存放字体的修改
         */
        public SparseArray<CharSequence> mTextArray = new SparseArray<>();
        /**
         * 存放点击事件
         */
        public SparseArray<View.OnClickListener> mClickArray = new SparseArray<>();
        /**
         * dialog Cancel监听
         */
        public DialogInterface.OnCancelListener mOnCancelListener;
        /**
         * dialog Dismiss监听
         */
        public DialogInterface.OnDismissListener mOnDismissListener;
        /**
         * dialog Key监听
         */
        public DialogInterface.OnKeyListener mOnKeyListener;

        public AlertParams(Context context, int themeResId) {
            this.mContext = context;
            this.mThemeResId = themeResId;
        }

        /**
         * 设置ALertController的DialogViewHelper，同时让 AlertController 实现Dialog底层功能之外的业务功能
         *
         * @param mAlert 构建过程中的Dialog的控制管理类
         */
        public void apply(AlertController mAlert) {
            // 1. 初始化DialogViewHelper，通过其来设置Dialog布局
            DialogViewHelper viewHelper = null;
            if (mViewLayoutResId != 0) {
                viewHelper = new DialogViewHelper(mContext, mViewLayoutResId);
            }

            if (mView != null) {
                viewHelper = new DialogViewHelper();
                viewHelper.setContentView(mView);
            }

            if (viewHelper == null) {
                throw new IllegalArgumentException("请使用setContentView()设置布局");
            }

            // 设置 AlertController的辅助类
            mAlert.setViewHelper(viewHelper);

            // 调用父类Dialog的方法给当前的dialog设置布局
            mAlert.getDialog().setContentView(viewHelper.getContentView());

            // 2.设置文本
            int textArraySize = mTextArray.size();
            for (int i = 0; i < textArraySize; i++) {
                mAlert.setText(mTextArray.keyAt(i), mTextArray.valueAt(i));
            }

            // 3.设置View的点击事件
            int clickArraySize = mClickArray.size();
            for (int i = 0; i < clickArraySize; i++) {
                mAlert.setOnclickListener(mClickArray.keyAt(i), mClickArray.valueAt(i));
            }

            // 4.配置自定义的效果（全屏、从底部弹出、默认动画）
            Window window = mAlert.getWindow();
            // 设置dialog的弹出位置
            window.setGravity(mGravity);

            // 设置动画
            if (mAnimations != 0) {
                window.setWindowAnimations(mAnimations);
            }

            // 设置dialog的宽高
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = mWidth;
            params.height = mHeight;
            window.setAttributes(params);
        }

    }
}
