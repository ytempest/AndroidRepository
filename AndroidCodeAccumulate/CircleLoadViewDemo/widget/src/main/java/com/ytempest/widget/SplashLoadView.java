package com.ytempest.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * @author ytempest
 *         Description：一个实现了旋转动画，聚合以及展开动画的加载View
 */
public class SplashLoadView extends View {

    private static final int ROTATION_TIME = 1450;
    private static final int MERGE_TIME = 200;
    private static final int EXPAND_TIME = 400;
    /**
     * 标志是否正在加载
     */
    private boolean mIsLoading = true;
    /**
     * 旋转的半径
     */
    private float mRotationRadius = 70;
    /**
     * 旋转的圆的半径
     */
    private float mCircleRadius = 10;
    /**
     * 加载界面的背景色
     */
    private int mBackgroundColor = Color.WHITE;

    private Paint mCirclePaint;
    private int[] mCircleColors;
    private int mCenterX;
    private int mCenterY;
    /**
     * 动画绘制的状态对象
     */
    private SplashStatus mSplashStatus;

    public SplashLoadView(Context context) {
        this(context, null);
    }

    public SplashLoadView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashLoadView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttr(context, attrs);

        initConfig();
    }

    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SplashLoadView);

        mRotationRadius = attributes.getDimension(R.styleable.SplashLoadView_rotationRadius, dpToPx(mRotationRadius));

        mCircleRadius = attributes.getDimension(R.styleable.SplashLoadView_circleRadius, dpToPx(mCircleRadius));

        mBackgroundColor = attributes.getColor(R.styleable.SplashLoadView_loadingBackground, mBackgroundColor);

        attributes.recycle();

    }

    private void initConfig() {
        // 设置背景为空，防止在动画切换过程中出现背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(null);
        } else {
            setBackgroundDrawable(null);
        }
        // 获取圆的颜色集
        mCircleColors = getContext().getResources().getIntArray(R.array.circle_colors);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);

    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 在onLayout中获取View的中心点，防止onMeasure方法多次回调导致测量的宽高不准确
        mCenterX = getMeasuredWidth() / 2;
        mCenterY = getMeasuredHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 如果当前没有动画状态，就默认使用旋转动画
        if (mSplashStatus == null) {
            mSplashStatus = new RotationStatus();
        }

        mSplashStatus.draw(canvas);

    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getContext().getResources().getDisplayMetrics());
    }


    /**
     * Description：动画状态
     */
    private interface SplashStatus {
        void draw(Canvas canvas);
    }

    /**
     * Description：旋转动画状态
     */
    private class RotationStatus implements SplashStatus {
        private float mCurrentAngle = 0;
        private ValueAnimator mRotation;
        private float mRadius;
        private float mAverageAngle;

        RotationStatus() {
            mAverageAngle = (float) (Math.PI * 2 / mCircleColors.length);
            mCirclePaint.setStyle(Paint.Style.FILL);
            mRadius = mRotationRadius;
            // 开启旋转动画
            startRotationAnimator();
        }

        private void startRotationAnimator() {
            // 角度从 0 到 360度
            mRotation = ValueAnimator.ofFloat(0, (float) Math.PI * 2);
            mRotation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentAngle = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mRotation.setInterpolator(new LinearInterpolator());
            mRotation.setRepeatCount(-1);
            mRotation.setDuration(ROTATION_TIME);
            mRotation.start();
        }

        @Override
        public void draw(Canvas canvas) {
            // 绘制加载界面的背景
            canvas.drawColor(mBackgroundColor);
            // 绘制圆点
            for (int i = 0; i < mCircleColors.length; i++) {
                mCirclePaint.setColor(mCircleColors[i]);
                float cx = (float) (mCenterX + mRadius * Math.cos(i * mAverageAngle + mCurrentAngle));
                float cy = (float) (mCenterY + mRadius * Math.sin(i * mAverageAngle + mCurrentAngle));
                canvas.drawCircle(cx, cy, mCircleRadius, mCirclePaint);
            }
        }

        /**
         * 加载完毕的时候开启聚合动画，通过不断改变旋转的半径达到聚合的效果，如果
         * 聚合动画结束就开启展开动画
         */
        public void startMergeAnimator() {
            /* 把下面的注释的代码放开，同时把插值器改为 AnticipateInterpolator(3.5f)
               可以实现另一种动画效果
            final float mAngle = (float) mRotation.getAnimatedValue();*/
            ValueAnimator mergeAnimator = ValueAnimator.ofFloat(mRotationRadius, 0);
            mergeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
//                    mCurrentAngle = mAngle;
                    mRadius = (float) animation.getAnimatedValue();
                }
            });
            mergeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // 停止旋转动画
                    mRotation.cancel();
                    // 切换动画状态到展开动画
                    mSplashStatus = new ExpandStatus();
                }
            });
            mergeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mergeAnimator.setDuration(MERGE_TIME);
            mergeAnimator.start();
        }
    }


    /**
     * Description：展开动画状态
     */
    private class ExpandStatus implements SplashStatus {

        private float mRadius;
        private float mMaxRadius;

        ExpandStatus() {
            // 求出中心点离屏幕左上角的距离，外加10像素是防止绘制完成后角落还有痕迹
            mMaxRadius = (float) Math.sqrt(Math.pow((getWidth() / 2), 2) + Math.pow(getHeight() / 2, 2)) + 10;
            mCirclePaint.setStyle(Paint.Style.STROKE);
            mCirclePaint.setColor(mBackgroundColor);
            startExpandAnimator();
        }

        private void startExpandAnimator() {
            ValueAnimator expandAnimator = ValueAnimator.ofFloat(0, mMaxRadius);
            expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            expandAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // 当展开动画结束后就将加载状态设置为未加载，同时隐藏该View
                    mIsLoading = false;
                    setVisibility(GONE);
                }
            });
            expandAnimator.setDuration(EXPAND_TIME);
            expandAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            // 获取画笔的宽度
            float stokeWidth = mMaxRadius - mRadius;
            mCirclePaint.setStrokeWidth(stokeWidth);
            // 这里之所以还要加上 stokeWidth/2 是因为画笔是以画笔的中心开始画的
            // 这样的话，实际的画出来的半径是 mRadius -stokeWidth/2，所以要加上
            canvas.drawCircle(mCenterX, mCenterY, mRadius + stokeWidth / 2, mCirclePaint);
        }
    }

    /**
     * 停止动画
     */
    public void stopLoading() {
        if (mIsLoading) {
            if (mSplashStatus instanceof RotationStatus) {
                ((RotationStatus) mSplashStatus).startMergeAnimator();
            }
        }
    }

    /**
     * 开启动画
     */
    public void startLoad() {
        if (!mIsLoading) {
            mIsLoading = true;
            setVisibility(VISIBLE);
            mSplashStatus = null;
            invalidate();
        }
    }
}

