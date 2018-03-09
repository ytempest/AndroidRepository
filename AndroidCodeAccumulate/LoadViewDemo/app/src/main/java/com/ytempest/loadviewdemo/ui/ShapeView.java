package com.ytempest.loadviewdemo.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.ytempest.loadviewdemo.R;

/**
 * @author ytempest
 *         Description：能改变自身形状的View
 */
public class ShapeView extends View {

    /**
     * 表示绘制的是圆形
     */
    private final static int SHAPE_CIRCLE = 100;
    /**
     * 表示绘制的是正方形
     */
    private final static int SHAPE_SQUARE = 200;
    /**
     * 表示绘制的三角形
     */
    private final static int SHAPE_TRIANGLE = 300;

    /**
     * 默认绘制的正方形
     */
    private int mCurrentShape = SHAPE_CIRCLE;

    private Context mContext;

    private int mWidth;

    private int mHeight;

    private Paint mShapePaint;

    public ShapeView(Context context) {
        this(context, null);
    }

    public ShapeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initPaint();
    }

    private void initPaint() {
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(true);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mCurrentShape) {
            case SHAPE_CIRCLE:
                mShapePaint.setColor(getColor(R.color.load_view_shape_circle_color));
                canvas.drawCircle(mWidth / 2, mHeight / 2, mWidth / 2, mShapePaint);
                break;
            case SHAPE_SQUARE:
                mShapePaint.setColor(getColor(R.color.load_view_shape_square_color));
                canvas.drawRect(0, 0, mWidth, mHeight, mShapePaint);
                break;
            case SHAPE_TRIANGLE:
                mShapePaint.setColor(getColor(R.color.load_view_shape_triangle_color));
                Path path = new Path();
                // 将path的点从当前View的左上角移动到指定的（x,y）位置
                path.moveTo(mWidth / 2, 0);
                // 向（x,y）点画线
                path.lineTo(0, (float) (Math.sqrt(3) / 2 * mHeight));
                path.lineTo(mWidth, (float) (Math.sqrt(3) / 2 * mHeight));
                // 连接path的起点合拢path
                path.close();
                canvas.drawPath(path, mShapePaint);
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
            case SHAPE_CIRCLE:
                mCurrentShape = SHAPE_SQUARE;
                break;
            case SHAPE_SQUARE:
                mCurrentShape = SHAPE_TRIANGLE;
                break;
            case SHAPE_TRIANGLE:
                mCurrentShape = SHAPE_CIRCLE;
                break;
            default:
                break;
        }
        invalidate();
    }


    /**
     * 开启 ShapeView的旋转动画
     */
    public void startRotateAnimation() {
        switch (mCurrentShape) {
            case SHAPE_CIRCLE:
                break;
            case SHAPE_SQUARE:
                ObjectAnimator squareRotation = ObjectAnimator.ofFloat(this, "rotation", 0, 180);
                squareRotation.setDuration(LoadView.ANIMATOR_TIME);
                squareRotation.start();
                break;
            case SHAPE_TRIANGLE:
                ObjectAnimator triangleRotation = ObjectAnimator.ofFloat(this, "rotation", 0, -120);
                triangleRotation.setDuration(LoadView.ANIMATOR_TIME);
                triangleRotation.start();
                break;
            default:
                break;
        }
    }


    private int getColor(int colorId) {
        return ContextCompat.getColor(mContext, colorId);
    }
}
