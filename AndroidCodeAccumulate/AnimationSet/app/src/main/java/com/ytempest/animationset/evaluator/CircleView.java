package com.ytempest.animationset.evaluator;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author ytempest
 *         Description：
 */
public class CircleView extends View {

    /** 圆的半径 = 50 */
    public static final float RADIUS = 50f;
    /** 当前点坐标 */
    public Point currentPoint;

    private Paint mPaint;

    public CircleView(Context context) {
        this(context, null);
    }
    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GRAY);

    }

    /**
     * 绘制逻辑:先在初始点画圆,通过监听当前坐标值(currentPoint)的变化,每次变化都调
     * 用onDraw()重新绘制圆,从而实现圆的抛物线动画效果
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // 如果当前点坐标为空(即首次绘图)
        if (currentPoint == null) {
            // 记录当前点(坐标是(50,50))
            currentPoint = new Point(RADIUS, RADIUS);
            // 在当前点画一个圆:圆心 = (50,50),半径 = 50
            float x = RADIUS;
            float y = RADIUS;
            canvas.drawCircle(x, y, RADIUS, mPaint);
        } else {
            // 在当前的点（这个点会在动画进行过程中变化）重新绘制圆
            float x = currentPoint.getX();
            float y = currentPoint.getY();
            canvas.drawCircle(x, y, RADIUS, mPaint);
        }

    }

    /**
     * 执行传入的属性动画
     * @param valueAnimator 要执行的属性动画
     */
    public void startValueAnimation(ValueAnimator valueAnimator) {

        // 设置属性动画监听，每当坐标值（Point对象）更新一次,该方法就会被调用一次
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /**
             * 从进行的属性动画中获取到 变化的属性值
             * @param animation 当前不断进行的属性动画
             */
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // 将每次变化后的坐标值（估值器PointEvaluator中evaluate() 返回的Piont对象值）赋给当前坐标值对象（currentPoint）
                // 从而更新当前坐标值（currentPoint）
                currentPoint = (Point) animation.getAnimatedValue();

                // invalidate()方法执行就会调用onDraw()一次,每次赋值后就重新绘制，从而实现动画效果
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 使用ObjectAnimator.ofObject() 方法没有传入起始值只传入结束值，那么
     * 在动画开始执行前会调用一次
     * @return 当面 View 的位置
     */
    public Point getCurrentPoint() {
        return currentPoint;
    }

    /**
     * 该方法在 ObjectAnimation 执行过程中不断被调用，参数 currentPoint 其实
     * 就是 PointEvaluator 估值器的 evaluate() 方法返回值
     * @param currentPoint 要设置的 View 的位置
     */
    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
        // 每次设置了新的View位置都刷新一次View
        invalidate();
    }


    public int getChangeColor() {
        return mPaint.getColor();
    }

    public void setChangeColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }
}
