package com.ytempest.slidingmenudemo.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

import com.nineoldandroids.view.ViewHelper;
import com.ytempest.slidingmenudemo.R;

/**
 * @author ytempest
 *         Description：侧滑菜单
 */
public class SlidingMenu extends HorizontalScrollView {

    private Context mContext;

    private View mMenuView, mContentView;

    private int mMenuWidth;

    private int mContentWidth;
    /**
     * 菜单变化的透明度
     */
    private float mMenuAlpha = 0.7f;
    /**
     * 菜单缩放的大小
     */
    private float mMenuScale = 0.7f;

    /**
     * 菜单位移的大小
     */
    private float mMenuTranslationX = 0.8f;
    /**
     * 内容缩放的大小
     */
    private float mContentScale = 0.7f;
    /**
     * 是否已经打开菜单
     */
    private boolean mMenuIsOpen = false;
    private GestureDetector mGestureDetector;

    public SlidingMenu(Context context) {
        this(context, null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        mGestureDetector = new GestureDetector(context, new GestureDetectorListener());

        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SlidingMenu);

        mContentWidth = getScreenWidth();

        float menuRightMargin = attributes.getDimension(R.styleable.SlidingMenu_menuRightMargin, spToPx(50));

        mMenuWidth = (int) (getScreenWidth() - menuRightMargin);

        mMenuAlpha = attributes.getFloat(R.styleable.SlidingMenu_menuAlpha, mMenuAlpha);

        mMenuTranslationX = attributes.getFloat(R.styleable.SlidingMenu_menuTranslationX, mMenuTranslationX);

        mMenuScale = attributes.getFloat(R.styleable.SlidingMenu_menuScale, mMenuScale);

        mContentScale = attributes.getFloat(R.styleable.SlidingMenu_contentScale, mContentScale);

        attributes.recycle();

    }

    private float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getResources().getDisplayMetrics());
    }

    /**
     * 获取手机屏幕的宽度
     */
    private int getScreenWidth() {
        WindowManager vm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        vm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }


    private class GestureDetectorListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // velocityX向右快速滑动会是正数，向左快速滑动是负数
            if (mMenuIsOpen) {
                if (velocityX < -2000) {
                    closeMenu();
                    return true;
                }
            } else {
                if (velocityX > +2000) {
                    openMenu();
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 拦截手势处理事件
        if (mGestureDetector.onTouchEvent(ev)) {
            return mGestureDetector.onTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mMenuIsOpen) {
                    if (ev.getX() > mMenuWidth) {
                        closeMenu();
                        return false;
                    }
                }

                // getScrollX()获取的是HorizontalScrollView进入手机屏幕后的X轴位置
                float currentScrollX = getScrollX();
                if (currentScrollX < (mMenuWidth / 3)) {
                    openMenu();
                } else {
                    closeMenu();
                }
                return false;
            default:
                break;

        }
        return super.onTouchEvent(ev);
    }

    /**
     * 整个布局加载完毕会调用该方法
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 这个获取的是 根布局  LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);

        mMenuView = container.getChildAt(0);

        mMenuView.getLayoutParams().width = mMenuWidth;

        mContentView = container.getChildAt(1);

        mContentView.getLayoutParams().width = mContentWidth;
    }

    /**
     * 滚动的时候会一直自动回调该方法
     */
    @Override
    protected void onScrollChanged(int left, int top, int oldl, int oldt) {
        super.onScrollChanged(left, top, oldl, oldt);

        //  // 计算变化的梯度值，变化范围 1.0f - 0f
        float scale = left * 1.0f / mMenuWidth;
        // 1、设置内容缩放
        float contentViewScale = mContentScale + (1 - mContentScale) * scale;
        // 设置缩放的中心点
        ViewHelper.setPivotX(mContentView, 0);
        ViewHelper.setPivotY(mContentView, mContentView.getHeight() / 2);
        ViewHelper.setScaleX(mContentView, contentViewScale);
        ViewHelper.setScaleY(mContentView, contentViewScale);

        // 2、设置菜单缩放
        float menuViewScale = mMenuScale + (1 - mMenuScale) * (1 - scale);
        ViewHelper.setPivotX(mMenuView, 0);
        ViewHelper.setPivotY(mMenuView, mMenuView.getHeight() / 2);
        ViewHelper.setScaleX(mMenuView, menuViewScale);
        ViewHelper.setScaleY(mMenuView, menuViewScale);

        // 3、设置菜单位移
        float menuViewTranslationX = mMenuTranslationX * left;
        ViewHelper.setTranslationX(mMenuView, menuViewTranslationX);

        // 4、设置菜单透明度变化
        float menuAlpha = mMenuAlpha + (1 - mMenuAlpha) * (1 - scale);
        ViewHelper.setAlpha(mMenuView, menuAlpha);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // 用来排放子布局的   等子View全部拜访完才能去滚动
        closeMenu();
    }


    private void closeMenu() {
        smoothScrollTo(mMenuWidth, 0);
        mMenuIsOpen = false;
    }

    private void openMenu() {
        smoothScrollTo(0, 0);
        mMenuIsOpen = true;
    }
}

