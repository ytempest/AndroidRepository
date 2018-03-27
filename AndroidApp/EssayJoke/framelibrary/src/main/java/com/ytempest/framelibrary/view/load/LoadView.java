package com.ytempest.framelibrary.view.load;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.ytempest.framelibrary.R;


/**
 * @author ytempest
 *         Description：加载数据时用于表示加载中的View
 */
public class LoadView extends LinearLayout {

    /**
     * 动画的执行时间
     */
    public static long ANIMATOR_TIME = 400;

    private ShapeView mShapeView;
    private View mShadowView;
    /**
     * 阴影部分变化的大小
     */
    private float mShadowScaleX = 0.4f;
    /**
     * 图形下落或上抛的高度
     */
    private float mAnimatorHeight;
    /**
     * 标识是否停止动画
     */
    private boolean isStopAnimator = false;

    public LoadView(Context context) {
        this(context, null);
    }

    public LoadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 把布局文件加载到本类中
        inflate(context, R.layout.ui_load_view, this);

        mShapeView = findViewById(R.id.iv_shape_view);

        mShadowView = findViewById(R.id.iv_shadow_view);

        mAnimatorHeight = getAnimationHeight();

        startFallAnimation();
    }

    /**
     * 开启图形的下落动画
     */
    private void startFallAnimation() {
        ObjectAnimator shapeFall = ObjectAnimator.ofFloat(mShapeView, "translationY", 0, mAnimatorHeight);
        shapeFall.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator shadowShrink = ObjectAnimator.ofFloat(mShadowView, "scaleX", 1.0f, mShadowScaleX);
        shadowShrink.setInterpolator(new AccelerateInterpolator());

        AnimatorSet fallAnimatorSet = new AnimatorSet();
        fallAnimatorSet.playTogether(shapeFall, shadowShrink);
        fallAnimatorSet.setDuration(ANIMATOR_TIME);
        fallAnimatorSet.start();

        fallAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 判断是否停止动画
                if (isStopAnimator) {
                    return;
                }
                // 改变图形View的形状
                mShapeView.exchangeShape();
                // 开启图形View的旋转动画
                mShapeView.startRotateAnimation();
                // 开启上抛动画
                startUpAnimation();
            }
        });

    }

    /**
     * 开启图形上抛动画
     */
    private void startUpAnimation() {
        ObjectAnimator shapeUp = ObjectAnimator.ofFloat(mShapeView, "translationY", mAnimatorHeight, 0);
        shapeUp.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator shadowExpand = ObjectAnimator.ofFloat(mShadowView, "scaleX", mShadowScaleX, 1.0f);
        shadowExpand.setInterpolator(new DecelerateInterpolator());

        AnimatorSet upAnimatorSet = new AnimatorSet();
        upAnimatorSet.playTogether(shapeUp, shadowExpand);
        upAnimatorSet.setDuration(ANIMATOR_TIME);
        upAnimatorSet.start();

        upAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isStopAnimator) {
                    return;
                }
                startFallAnimation();
            }
        });
    }

    /**
     * 获取图形动画执行的高度
     *
     * @return 高度的像素值
     */
    private float getAnimationHeight() {
        MarginLayoutParams shapeViewMargin = (MarginLayoutParams) mShapeView.getLayoutParams();
        MarginLayoutParams shadowViewMargin = (MarginLayoutParams) mShadowView.getLayoutParams();

        return shapeViewMargin.bottomMargin + shadowViewMargin.topMargin;
    }

    /**
     * 重写该方法，对LoadView做一下内存优化
     */
    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            // 停止所有动画
            stopAllAnimator();
            // 获取 LoadView所在的父布局，然后在父布局中移除LoadView
            ViewGroup parent = (ViewGroup) this.getParent();
            if (parent != null) {
                parent.removeView(this);
            }
        }
    }

    /**
     * 当回退Activity的时候，该View从Window中移除时会调用该方法
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 停止所有动画
        stopAllAnimator();
    }

    /**
     * 停止动画
     */
    private void stopAllAnimator() {
        isStopAnimator = true;
    }
}
