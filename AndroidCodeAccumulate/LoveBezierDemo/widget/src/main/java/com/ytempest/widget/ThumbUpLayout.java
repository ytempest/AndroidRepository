package com.ytempest.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * @author ytempest
 *         Description：仿QQ点赞效果的实现
 */
public class ThumbUpLayout extends RelativeLayout {

    private Context mContext;
    /**
     * 贝塞尔曲线的宽度
     */
    private float mDiffuseWidth = 100;
    /**
     * 贝塞尔曲线的高度
     */
    private float mDiffuseHeight = 150;
    /**
     * 动画开始的圆点的X轴位移点
     */
    private float mOffsetX = 0;
    /**
     * 动画开始的圆点的Y轴位移点
     */
    private float mOffsetY = 0;

    private LayoutParams mLayoutParams;
    private int[] mImages;
    private Random mRandom;

    /**
     * 图片的宽高
     */
    private int mDrawableWidth;
    private int mDrawableHeight;
    private PointF mPoint0;
    private PointF mPoint1;
    private PointF mPoint2;
    private PointF mPoint3;

    public ThumbUpLayout(Context context) {
        this(context, null);
    }

    public ThumbUpLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbUpLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;

        initAttr(context, attrs);

        initConfig();

    }


    private void initAttr(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ThumbUpLayout);

        mDiffuseWidth = attributes.getDimension(R.styleable.ThumbUpLayout_diffuseWidth, dpToPx(mDiffuseWidth));

        mDiffuseHeight = attributes.getDimension(R.styleable.ThumbUpLayout_diffuseHeight, dpToPx(mDiffuseHeight));

        mOffsetX = attributes.getDimension(R.styleable.ThumbUpLayout_offsetX, dpToPx(mOffsetX));

        mOffsetY = attributes.getDimension(R.styleable.ThumbUpLayout_offsetY, dpToPx(mOffsetY));

        attributes.recycle();

    }

    private void initConfig() {
        mLayoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayoutParams.addRule(CENTER_HORIZONTAL);
        mLayoutParams.addRule(ALIGN_PARENT_BOTTOM);

        // 初始化图片数组
        mImages = new int[]{R.drawable.blue, R.drawable.pink, R.drawable.yellow, R.drawable.purple};

        mRandom = new Random();

        Drawable drawable = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = mContext.getResources().getDrawable(mImages[0], null);
        } else {
            drawable = mContext.getResources().getDrawable(mImages[0]);
        }
        // 获取图片的宽度和高度
        mDrawableWidth = drawable.getIntrinsicWidth();
        mDrawableHeight = drawable.getIntrinsicHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // 初始化贝塞尔曲线的四个点
        initPointF(width, height);
    }

    private void initPointF(int width, int height) {
        float originX = width / 2 + mOffsetX - mDrawableWidth / 2;
        float originY = height + mOffsetY - mDrawableHeight;

        // 设置贝塞尔曲线的起点
        mPoint0 = new PointF(originX, originY);

        // 设置贝塞尔曲线的第一个控制点
        float p1X = originX - 0.618f * mDiffuseWidth / 2 + mRandom.nextInt((int) (0.618f * mDiffuseWidth));
        float p1Y = originY - mDiffuseHeight * 0.382f;
        mPoint1 = new PointF(p1X, p1Y);

        // 设置贝塞尔曲线的第二个控制点
        float p2X = originX - 0.382f * mDiffuseWidth / 2 + mRandom.nextInt((int) (0.382f * mDiffuseWidth));
        float p2Y = originY - mDiffuseHeight * 0.618f;
        mPoint2 = new PointF(p2X, p2Y);

        // 设置贝塞尔曲线的终点
        float p3X = originX - mDiffuseWidth / 2 + mRandom.nextInt((int) mDiffuseWidth);
        float p3Y = originY - mDiffuseHeight;
        mPoint3 = new PointF(p3X, p3Y);
    }

    /**
     * 获取点赞效果的View
     */
    private ImageView getThumpUpView() {
        ImageView imageView = new ImageView(mContext);

        imageView.setLayoutParams(mLayoutParams);

        imageView.setImageResource(mImages[mRandom.nextInt(mImages.length)]);

        return imageView;
    }

    /**
     * 每执行一次就实现一次点赞效果
     */
    public void onThumpUp() {
        ImageView imageView = getThumpUpView();
        addView(imageView);
        // 开启点赞动画
        startThumpUpAnimation(imageView);
    }

    private void startThumpUpAnimation(final ImageView imageView) {
        // 开启View进入界面的动画效果
        AnimatorSet enterAnimatorSet = getEnterAnimationSet(imageView, 300);
        enterAnimatorSet.start();

        // 贝塞尔估值器
        BezierTypeEvaluator bezierTypeEvaluator = new BezierTypeEvaluator(mPoint1, mPoint2);
        ValueAnimator bezierAnimator = ValueAnimator.ofObject(bezierTypeEvaluator, mPoint0, mPoint3);
        bezierAnimator.setTarget(imageView);
        bezierAnimator.setDuration(1800);
        bezierAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                imageView.setX(pointF.x);
                imageView.setY(pointF.y);

                imageView.setAlpha(1 - animation.getAnimatedFraction() + 0.2f);
            }
        });
        bezierAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                removeView(imageView);
            }
        });
        bezierAnimator.start();
    }

    private AnimatorSet getEnterAnimationSet(ImageView imageView, long duration) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator alpha = ObjectAnimator.ofFloat(imageView,
                "alpha", 0.3f, 1.0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView,
                "scaleX", 0.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView,
                "scaleY", 0.3f, 1.0f);

        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.setDuration(duration);

        return animatorSet;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, mContext.getResources().getDisplayMetrics());
    }
}
