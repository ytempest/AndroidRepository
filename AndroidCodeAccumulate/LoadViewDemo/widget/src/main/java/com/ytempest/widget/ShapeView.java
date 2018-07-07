package com.ytempest.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author ytempest
 *         Description：能改变自身形状的View
 */
public class ShapeView extends View {

    private static final int SHAPE_CIRCLE = 100;
    private static final int SHAPE_SQUARE = 200;
    private static final int SHAPE_TRIANGLE = 300;

    private int mCurrentShape = SHAPE_SQUARE;

    private Context mContext;

    private int mWidth;
    private int mHeight;

    private Paint mShapePaint;
    private Path mTrianglePath;

    public ShapeView(Context context) {
        this(context, null);
    }

    public ShapeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initConfig();
    }

    private void initConfig() {
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mCurrentShape) {
            case SHAPE_CIRCLE:
                mShapePaint.setColor(getColor(R.color.shape_circle_color));
                canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mShapePaint);
                break;
            case SHAPE_SQUARE:
                mShapePaint.setColor(getColor(R.color.shape_square_color));
                canvas.drawRect(0, 0, mWidth, mHeight, mShapePaint);
                break;
            case SHAPE_TRIANGLE:
                mShapePaint.setColor(getColor(R.color.shape_triangle_color));
                if (mTrianglePath == null) {
                    mTrianglePath = getTrianglePath();
                }
                canvas.drawPath(mTrianglePath, mShapePaint);
                break;
            default:
                break;
        }
    }

    /**
     * 改变ShapeView的形状，重新绘制
     */
    public void exchangeShape() {
        switch (mCurrentShape) {
            case SHAPE_SQUARE:
                mCurrentShape = SHAPE_CIRCLE;
                break;
            case SHAPE_CIRCLE:
                mCurrentShape = SHAPE_TRIANGLE;
                break;
            case SHAPE_TRIANGLE:
                mCurrentShape = SHAPE_SQUARE;
                break;
            default:
                break;
        }
        invalidate();
    }


    /**
     * 开启 ShapeView的旋转动画
     */
    public void startRotateAnimation(long duration) {
        switch (mCurrentShape) {
            case SHAPE_SQUARE:
                ObjectAnimator squareRotation = ObjectAnimator.ofFloat(this, "rotation", 0, 180);
                squareRotation.setDuration(duration);
                squareRotation.start();
                break;

            case SHAPE_CIRCLE:
                break;

            case SHAPE_TRIANGLE:
                ObjectAnimator triangleRotation = ObjectAnimator.ofFloat(this, "rotation", 0, -120);
                triangleRotation.setDuration(duration);
                triangleRotation.start();
                break;
            default:
                break;
        }
    }


    private int getColor(int colorId) {
        return ContextCompat.getColor(mContext, colorId);
    }

    /**
     * 获取一个绘制三角形需要的Path画笔路径
     */
    public Path getTrianglePath() {
        Path path = new Path();
        // 将path的点从当前View的左上角移动到指定的（x,y）位置
        path.moveTo(mWidth / 2, 0);
        // 向（x,y）点画线
        path.lineTo(0, (float) (Math.sqrt(3) / 2 * mWidth));
        path.lineTo(mWidth, (float) (Math.sqrt(3) / 2 * mWidth));
        // 连接path的起点合拢path
        path.close();
        return path;
    }
}
