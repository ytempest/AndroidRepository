package com.ytempest.bezierdemo.bezier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author ytempest
 *         Description：实现了贝塞尔曲线的仿QQ消息拖拽的一个View
 */
public class BezierView extends View {

    private Paint mBezierPaint;
    private Paint mCirclePaint;

    private int mBezierColor = Color.parseColor("#FF9090FF");
    private int mCircleColor = Color.parseColor("#FF6078FF");

    private PointF mDragPoint;
    private PointF mFixationPoint;

    private float mDragPointRadius = 13;
    private float mFixationPointRadius = 7;
    private float mFixationPointMax;
    private float mFixationPointMin = 5;
    private int mDrawRatio = 40;

    public BezierView(Context context) {
        this(context, null);
    }

    public BezierView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initConfig();
    }


    private void initConfig() {
        mDragPointRadius = dpToPx(mDragPointRadius);
        mFixationPointRadius = dpToPx(mFixationPointRadius);
        mFixationPointMax = mFixationPointRadius;
        mFixationPointMin = dpToPx(mFixationPointMin);

        mBezierPaint = new Paint();
        mBezierPaint.setAntiAlias(true);
        mBezierPaint.setDither(true);
        mBezierPaint.setColor(mBezierColor);

        mCirclePaint = new Paint(mBezierPaint);
        mCirclePaint.setColor(mCircleColor);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getContext().getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDragPoint == null || mFixationPoint == null) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDragPointRadius, mCirclePaint);
            return;
        }

        // 获取拖拽的距离
        float drawDistance = getPointDistance(mDragPoint, mFixationPoint);
        mFixationPointRadius = mFixationPointMax - drawDistance / mDrawRatio;
        if (mFixationPointRadius > mFixationPointMin) {
            Path path = getBezierPath(mFixationPoint, mDragPoint);
            // 绘制贝塞尔曲线
            canvas.drawPath(path, mBezierPaint);
            // 绘制内圆
            canvas.drawCircle(mFixationPoint.x, mFixationPoint.y, mFixationPointRadius, mCirclePaint);
        }
        // 绘制外圆
        canvas.drawCircle(mDragPoint.x, mDragPoint.y, mDragPointRadius, mCirclePaint);

    }

    private Path getBezierPath(PointF startPoint, PointF endPoint) {
        Path bezierPath = new Path();

        float dx = endPoint.x - startPoint.x;
        float dy = endPoint.y - startPoint.y;

        float tanA = dy / dx;

        double arcTanA = Math.atan(tanA);

        float p0X = (float) (startPoint.x + Math.sin(arcTanA) * mFixationPointRadius);
        float p0Y = (float) (startPoint.y - Math.cos(arcTanA) * mFixationPointRadius);

        float p1X = (float) (endPoint.x + Math.sin(arcTanA) * mDragPointRadius);
        float p1Y = (float) (endPoint.y - Math.cos(arcTanA) * mDragPointRadius);

        float p2X = (float) (endPoint.x - Math.sin(arcTanA) * mDragPointRadius);
        float p2Y = (float) (endPoint.y + Math.cos(arcTanA) * mDragPointRadius);

        float p3X = (float) (startPoint.x - Math.sin(arcTanA) * mFixationPointRadius);
        float p3Y = (float) (startPoint.y + Math.cos(arcTanA) * mFixationPointRadius);

        // 获取贝塞尔曲线的控制点
        PointF controlPoint = getControlPoint(startPoint, endPoint, (1 - 0.618f));

        bezierPath.moveTo(p0X, p0Y);
        bezierPath.quadTo(controlPoint.x, controlPoint.y, p1X, p1Y);
        bezierPath.lineTo(p2X, p2Y);
        bezierPath.quadTo(controlPoint.x, controlPoint.y, p3X, p3Y);
        bezierPath.close();

        return bezierPath;
    }

    /**
     * 获取 startPoint点和 endPoint点构成的直线上，占据这条直线 ratio比率部分的点
     *
     * @return 一个点
     */
    private PointF getControlPoint(PointF startPoint, PointF endPoint, float ratio) {
        PointF pointF = new PointF();
        pointF.x = startPoint.x + (endPoint.x - startPoint.x) * ratio;
        pointF.y = startPoint.y + (endPoint.y - startPoint.y) * ratio;
        return pointF;
    }

    /**
     * 获取两个点的距离
     */
    private float getPointDistance(PointF dragPoint, PointF fixationPoint) {
        return (float) Math.sqrt(Math.pow((dragPoint.x - fixationPoint.x), 2)
                + Math.pow((dragPoint.y - fixationPoint.y), 2));

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float currentX = event.getX();
        float currentY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initPoint(currentX, currentY);
                break;

            case MotionEvent.ACTION_MOVE:
                updatePoint(currentX, currentY);
                break;

            case MotionEvent.ACTION_UP:
                break;

            default:
                break;
        }

        invalidate();
        return true;
    }

    private void updatePoint(float currentX, float currentY) {
        mDragPoint.x = currentX;
        mDragPoint.y = currentY;
    }

    private void initPoint(float downX, float downY) {
        mDragPoint = new PointF(downX, downY);
        mFixationPoint = new PointF(downX, downY);
    }
}
