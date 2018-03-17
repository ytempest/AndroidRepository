package com.ytempest.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * @author ytempest
 *         Description：绘制密码的九宫格View
 */
public class LockView extends View {

    private Paint mNormalPaint;
    private Paint mSelectPaint;
    private Paint mErrorPaint;
    private Paint mLinePaint;

    private int mNormalColor = Color.parseColor("#FFB1AFAF");
    private int mSelectColor = Color.parseColor("#DD1AB9C8");
    private int mErrorColor = Color.parseColor("#DDF1685E");

    private float mCircleSize = 3.5f;
    private float mInnerRadius = 1;
    private float mOuterRadius = 30;
    /**
     * 记录当前触摸的位置，用于绘制直线
     */
    private float mCurrentX = 0;
    private float mCurrentY = 0;
    /**
     * 标志绘制的密码是否错误
     */
    private boolean mIsError = false;
    /**
     * 标识是否完成绘制密码
     */
    private boolean mIsTouchFinish = false;

    private Point[][] mPoints = new Point[3][3];
    private ArrayList<Point> mSelectPointList = new ArrayList<>();
    private OnLockPressListener mOnLockPressListener;

    public LockView(Context context) {
        this(context, null);
    }

    public LockView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);

        // 初始化画笔
        initPaint();

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.LockView);

        mNormalColor = attributes.getColor(R.styleable.LockView_normalColor, mNormalColor);

        mSelectColor = attributes.getColor(R.styleable.LockView_selectColor, mSelectColor);

        mErrorColor = attributes.getColor(R.styleable.LockView_errorColor, mErrorColor);

        mCircleSize = attributes.getDimension(R.styleable.LockView_circleSize, dpToPx(mCircleSize));

        mInnerRadius = attributes.getDimension(R.styleable.LockView_innerCircleRadius, dpToPx(mInnerRadius));

        mOuterRadius = attributes.getDimension(R.styleable.LockView_outerCircleRadius, dpToPx(mOuterRadius));

        attributes.recycle();
    }

    private void initPaint() {
        mNormalPaint = new Paint();
        mNormalPaint.setStyle(Paint.Style.STROKE);
        mNormalPaint.setAntiAlias(true);
        mNormalPaint.setColor(mNormalColor);
        mNormalPaint.setStrokeWidth(mCircleSize);

        mSelectPaint = new Paint(mNormalPaint);
        mSelectPaint.setColor(mSelectColor);

        mErrorPaint = new Paint(mNormalPaint);
        mErrorPaint.setColor(mErrorColor);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth(mCircleSize);
        mLinePaint.setColor(mSelectColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        // 重设LockView的宽度和高度，选两个的最小值作为LockView正方形的边长
        if (width > height) {
            width = height;
        } else {
            height = width;
        }
        setMeasuredDimension(width, height);
        // 初始化所有的圆点
        initPoint();
    }

    private void initPoint() {
        // 获取LockView实际占据的宽度，即去掉padding
        float width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        // 一个圆点占据的宽度
        float pointSize = width / 3;
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                float cx = getPaddingLeft() + j * pointSize + pointSize / 2;
                float cy = getPaddingTop() + i * pointSize + pointSize / 2;
                // 圆点的位置，范围：1~9
                int position = i * mPoints[i].length + j + 1;
                mPoints[i][j] = new Point(position, cx, cy, mInnerRadius, mOuterRadius);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLine(canvas);
        drawPoint(canvas);
    }

    /**
     * 绘制直线，根据手指滑动的位置，不断绘制直线
     */
    private void drawLine(Canvas canvas) {
        // 如果密码错误
        if (mIsError) {
            mLinePaint.setColor(mErrorColor);
            drawLineByPaint(canvas, mLinePaint);
        } else {
            mLinePaint.setColor(mSelectColor);
            drawLineByPaint(canvas, mLinePaint);
        }
    }

    /**
     * 根据不同的画笔绘制直线
     */
    private void drawLineByPaint(Canvas canvas, Paint paint) {
        if (mSelectPointList.size() != 0) {
            Point lastPoint = mSelectPointList.get(0);
            // 将选择了的圆点首尾一个一个用直线连接起来
            for (int i = 0; i < mSelectPointList.size() - 1; i++) {
                Point startPoint = mSelectPointList.get(i);
                lastPoint = mSelectPointList.get(i + 1);
                canvas.drawLine(startPoint.mCenterX, startPoint.mCenterY, lastPoint.mCenterX, lastPoint.mCenterY, paint);
            }
            // 将最后一个圆点的中心绘制一条直线连接到当前手指触摸的位置
            canvas.drawLine(lastPoint.mCenterX, lastPoint.mCenterY, mCurrentX, mCurrentY, paint);
        }

    }

    /**
     * 绘制所有圆点
     */
    private void drawPoint(Canvas canvas) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                if (mSelectPointList.contains(mPoints[i][j])) {
                    // 如果密码错误就绘制红色的圆点
                    if (mIsError) {
                        drawPointByPaint(canvas, mPoints[i][j], mErrorPaint);
                    } else {
                        drawPointByPaint(canvas, mPoints[i][j], mSelectPaint);
                    }
                    continue;
                }
                drawPointByPaint(canvas, mPoints[i][j], mNormalPaint);
            }
        }
        // 如果手指已经离开屏幕就清除当前的选择的圆点
        if (mIsTouchFinish) {
            mSelectPointList.clear();
            mIsError = false;
            mIsTouchFinish = false;
        }
    }

    /**
     * 根据画笔绘圆点
     */
    private void drawPointByPaint(Canvas canvas, Point point, Paint paint) {
        // 绘制内圆
        canvas.drawCircle(point.mCenterX, point.mCenterY,
                point.mInnerRadius, paint);
        // 绘制外圆
        canvas.drawCircle(point.mCenterX, point.mCenterY,
                point.mOuterRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录当前的位置，绘制直线的时候要用到
                mCurrentX = event.getX();
                mCurrentY = event.getY();
                if (!isTouchInCircle(mCurrentX, mCurrentY)) {
                    return false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mSelectPointList.size() < 9) {
                    mCurrentX = event.getX();
                    mCurrentY = event.getY();
                    isTouchInCircle(mCurrentX, mCurrentY);
                } else {
                    // 如果已经选择了9个圆点就设置当前的位置为最后一个圆点的中心
                    restoreCurrentXY();
                }
                break;

            case MotionEvent.ACTION_UP:
                // 当手指离开屏幕，将当前位置设置为最后一个圆点的中心
                restoreCurrentXY();
                // 事件回调
                if (mOnLockPressListener != null) {
                    String selectPoint = "";
                    for (Point point : mSelectPointList) {
                        selectPoint += point.mPosition;
                    }
                    // 记录密码是否正确
                    mIsError = !mOnLockPressListener.onFinish(selectPoint);
                }
                mIsTouchFinish = true;
                break;
            default:
                break;
        }
        // 每一次触摸都重绘界面
        invalidate();
        return true;
    }

    /**
     * 设置当前位置为选择圆点集合的最后一个点的中心
     */
    private void restoreCurrentXY() {
        mCurrentX = mSelectPointList.get(mSelectPointList.size() - 1).mCenterX;
        mCurrentY = mSelectPointList.get(mSelectPointList.size() - 1).mCenterY;
    }


    /**
     * 判断指点的位置是否在九个圆的其中一个
     *
     * @param currentX 手指触摸位置的X轴坐标
     * @param currentY 手指触摸位置的Y轴坐标
     * @return 返回true则表示在圆点中，否则不在
     */
    private boolean isTouchInCircle(float currentX, float currentY) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point point = mPoints[i][j];
                // 如果触摸点的位置离圆点的距离系小于圆的半径加上圆的外径宽度的一半
                if (Math.sqrt(Math.pow((currentX - point.mCenterX), 2)
                        + Math.pow((currentY - point.mCenterY), 2))
                        <= point.mOuterRadius + mCircleSize / 2) {
                    // 如果该点还没有选择
                    if (!mSelectPointList.contains(point)) {
                        // 如果该点和上一个点之间还隔了一个点就把这个点也加到集合
                        addTheSkipPointIfHave(point);
                        mSelectPointList.add(point);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 遍历九个点检查selectPoint这个点与集合中的最后一个点的直线中心是否还有一个点，
     * 如果有就把这个点加上
     */
    private void addTheSkipPointIfHave(Point selectPoint) {
        if (mSelectPointList.size() >= 1) {
            Point startPoint = mSelectPointList.get(mSelectPointList.size() - 1);
            for (int i = 0; i < mPoints.length; i++) {
                for (int j = 0; j < mPoints[i].length; j++) {
                    if (mSelectPointList.contains(mPoints[i][j])) {
                        continue;
                    }
                    if (isInSameLine(startPoint, selectPoint, mPoints[i][j])
                            && isCenterOfLine(startPoint, selectPoint, mPoints[i][j])) {
                        mSelectPointList.add(mPoints[i][j]);
                    }
                }
            }
        }
    }


    /**
     * 判断 point点是否在 startPoint点和 endPoint点形成的直线上
     */
    private boolean isInSameLine(Point startPoint, Point endPoint, Point point) {
        float distance = Math.abs(point.mCenterY * (startPoint.mCenterX - endPoint.mCenterX)
                - point.mCenterX * (startPoint.mCenterY - endPoint.mCenterY)
                - endPoint.mCenterY * (startPoint.mCenterX - endPoint.mCenterX)
                + endPoint.mCenterX * (startPoint.mCenterY - endPoint.mCenterY));
        // 为了防止数据有细微的差距，做一些处理
        return distance <= 10;
    }

    /**
     * 判断 point点是否在 startPoint点和 endPoint点形成的直线的中点
     */
    public boolean isCenterOfLine(Point startPoint, Point endPoint, Point target) {
        float distanceOne = getPointDistance(startPoint, target);
        float distanceTwo = getPointDistance(target, endPoint);
        // 为了防止数据有细微的差距，做一些处理
        return Math.abs(distanceOne - distanceTwo) < 10;
    }

    private float getPointDistance(Point startPoint, Point endPoint) {
        return (float) Math.sqrt(Math.pow((startPoint.mCenterX - endPoint.mCenterX), 2)
                + Math.pow((startPoint.mCenterY - endPoint.mCenterY), 2));
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getContext().getResources().getDisplayMetrics());
    }


    public void setOnLockPressListener(OnLockPressListener lockPressListener) {
        this.mOnLockPressListener = lockPressListener;
    }

    /**
     * 重置LockView，重新绘制默认状态的LockView
     */
    public void restoreLockView() {
        mSelectPointList.clear();
        mIsError = false;
        mIsTouchFinish = false;
        invalidate();
    }

    public interface OnLockPressListener {

        /**
         * 当触摸结束，即手指离开屏幕就会调用该方法
         *
         * @param selectPoint 密码
         * @return 返回true表示这段字符串正确，否则错误
         */
        boolean onFinish(String selectPoint);
    }

    private class Point {
        public int mPosition;
        public float mCenterX;
        public float mCenterY;
        public float mInnerRadius;
        public float mOuterRadius;

        public Point(int position, float centerX, float centerY, float innerRadius, float outerRadius) {
            mPosition = position;
            mCenterX = centerX;
            mCenterY = centerY;
            mInnerRadius = innerRadius;
            mOuterRadius = outerRadius;
        }
    }

}

