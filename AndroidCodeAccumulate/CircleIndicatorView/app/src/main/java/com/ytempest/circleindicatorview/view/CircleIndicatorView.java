package com.ytempest.circleindicatorview.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.ytempest.circleindicatorview.R;

import java.text.DecimalFormat;

/**
 * @author Administrator
 */
public class CircleIndicatorView extends View {

    private final String leftText = "￥";

    // 圆弧开始的角度
    private final float startAngle = 150;
    // 圆弧绘制的角度大小
    private final float sweepAngle = 240;
    // 外层弧线的厚度
    private final float lineWidth = 5f;
    private final float bitmapSize = dpToPx(lineWidth);

    // 外层弧线的画笔
    private Paint linePaint;
    // 虚线画笔
    private Paint dashPaint;
    // 中间文字的画笔
    private Paint textPaint;

    private int mCenter = 0;
    private int mRadius = 0;
    private float indexValue;
    private float inSweepAngle;
    private int mLargeTextSize;
    private int mSmallTextSize;
    private int mInLineColor;
    private int mOutLineColor;
    private int dashedLineColor;
    private int mTextColor;
    private RectF lineRectF;
    private RectF dashedRectF;
    private DecimalFormat decimalFormat;
    private Paint circlePaint;
    private Bitmap dotBitmap;
    private String title = "可用额度";


    public CircleIndicatorView(Context context) {
        this(context, null);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CircleIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleIndicatorView);
        mSmallTextSize = (int) typedArray.getDimension(R.styleable.CircleIndicatorView_smallTextSize, dpToPx(15));
        mLargeTextSize = (int) typedArray.getDimension(R.styleable.CircleIndicatorView_largeTextSize, dpToPx(18));
        mInLineColor = typedArray.getColor(R.styleable.CircleIndicatorView_inLineColor,
                Color.parseColor("#bcbcbc"));
        mOutLineColor = typedArray.getColor(R.styleable.CircleIndicatorView_outLineColor, Color.WHITE);
        mTextColor = typedArray.getColor(R.styleable.CircleIndicatorView_textColor,
                Color.parseColor("#37c1eb"));
        dashedLineColor = typedArray.getColor(R.styleable.CircleIndicatorView_dashedLineColor, Color.WHITE);
        typedArray.recycle();

        initPaint();

        initConfig();

    }

    /**
     * 初始化画笔
     */
    public void initPaint() {
        linePaint = getPaint(lineWidth, mInLineColor);
        dashPaint = getPaint(3, dashedLineColor);

        setLayerType(View.LAYER_TYPE_SOFTWARE, dashPaint);
        PathEffect effects = new DashPathEffect(new float[]{20, 6}, 0);
        dashPaint.setPathEffect(effects);

        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setColor(mTextColor);
        textPaint.setTextSize(mLargeTextSize);

        //绘制发光的小圆点
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);//抗锯齿功能
    }

    private void initConfig() {
        lineRectF = new RectF();
        dashedRectF = new RectF();

        decimalFormat = new DecimalFormat(".00");

        Bitmap pointBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_point_indicator);

        // 获得图片的宽高
        int width = pointBitmap.getWidth();
        int height = pointBitmap.getHeight();
        // 设置想要的大小
        float newWidth = bitmapSize * 2;
        float newHeight = bitmapSize * 2;
        // 计算缩放比例
        float scaleWidth = newWidth / width;
        float scaleHeight = newHeight / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        // 得到新的图片
        dotBitmap = Bitmap.createBitmap(pointBitmap, 0, 0, width, height, matrix, true);

        pointBitmap.recycle();
    }


    private Paint getPaint(float width, int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        return paint;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }


    private int measureWidth(int widthMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
        }
        return result;
    }


    private int measureHeight(int heightMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                break;
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCenter = getMeasuredWidth() / 2;
        mRadius = getMeasuredHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 设置外层弧线的边界
        lineRectF.set(mCenter - mRadius + getPaddingLeft(),
                mCenter - mRadius + getPaddingTop(),
                mCenter + mRadius - getPaddingRight(),
                mCenter + mRadius - getPaddingBottom());

        // 设置内虚线弧线的边界
        dashedRectF.set(mCenter - mRadius + 20 + getPaddingLeft(),
                mCenter - mRadius + 20 + getPaddingTop(),
                mCenter + mRadius - 20 - getPaddingRight(),
                mCenter + mRadius - 20 - getPaddingBottom());

        // 绘制弧线弧线
        canvas.drawArc(dashedRectF, startAngle, sweepAngle, false, dashPaint);

        // 绘制外层弧线的下层弧线
        linePaint.setColor(mInLineColor);
        canvas.drawArc(lineRectF, startAngle, sweepAngle, false, linePaint);

        // 绘制外层弧线的上层层弧线
        linePaint.setColor(mOutLineColor);
        canvas.drawArc(lineRectF, startAngle, inSweepAngle, false, linePaint);

        // 构造方法的字符格式这里如果小数不足2位,会以0补足.
        String value = decimalFormat.format(indexValue);
        String[] numberStr = value.split("\\.");


        // 中间的文字
        textPaint.setTextSize(mLargeTextSize);
        String centerText = numberStr[0];
        int centerTextWidth = (int) textPaint.measureText(centerText);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        int centerTextHeight = (int) (fontMetrics.bottom - fontMetrics.top);

        // getMeasuredHeight() / 2 + textHeight / 2 - fontMetrics.bottom 作用是求出Text的基线位置
        float baseLine = getMeasuredHeight() / 2 + centerTextHeight / 2 - fontMetrics.bottom;
        canvas.drawText(centerText, getMeasuredWidth() / 2,
                baseLine, textPaint);

        //左边的文字
        int leftTextWidth = (int) textPaint.measureText(leftText);
        canvas.drawText(leftText, getMeasuredWidth() / 2 - centerTextWidth / 2 - leftTextWidth / 2,
                baseLine, textPaint);

        // 右边的文字
        textPaint.setTextSize(mSmallTextSize);
        String rightText = "." + numberStr[1];
        int rightTextWidth = (int) textPaint.measureText(rightText);
        canvas.drawText(rightText, getMeasuredWidth() / 2 + centerTextWidth / 2 + rightTextWidth / 2,
                baseLine, textPaint);

        // 中间文字顶部的文字
        Paint.FontMetrics titleFontMetrics = textPaint.getFontMetrics();
        int upTextHeight = (int) (titleFontMetrics.bottom - titleFontMetrics.top);

        canvas.drawText(title, getMeasuredWidth() / 2,
                getMeasuredHeight() / 2 - upTextHeight / 2 - titleFontMetrics.bottom,
                textPaint);

        // 将画布移动到View的中心位置
        //画布平移到圆心
        canvas.translate(getWidth() / 2, getHeight() / 2);

        // 旋转画布，使小圆点和进度弧线重合
        // startAngle - 90：小圆点需要修正的角度，以便和进度弧线重合
        canvas.rotate(inSweepAngle + startAngle - 90);
        // 再次平移画布至小圆点绘制的位置
        canvas.translate(0, getHeight() / 2 - getPaddingLeft());

        canvas.drawBitmap(dotBitmap, -bitmapSize, -bitmapSize, circlePaint);
        canvas.rotate(-(inSweepAngle + startAngle - 90));
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, getContext().getResources().getDisplayMetrics());

    }

    public void goToPoint(float value) {
 /*       if (value < 20)
            outPaint.setColor(ContextCompat.getColor(getContext(), R.color.red));
        else
            outPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));*/
        float inSweepAngle = sweepAngle * value / 100;
        ValueAnimator angleAnim = ValueAnimator.ofFloat(0f, inSweepAngle);
        float inValue = value * 8888 / 100;
        ValueAnimator valueAnim = ValueAnimator.ofFloat(0, inValue);
        angleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float currentValue = (float) valueAnimator.getAnimatedValue();
                setInSweepAngle(currentValue);
                invalidate();
            }
        });
        valueAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float currentValue = (float) valueAnimator.getAnimatedValue();
                setIndexValue(currentValue);
                invalidate();
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(3000);
        animatorSet.playTogether(angleAnim, valueAnim);
        animatorSet.start();
    }

    public void setIndexValue(float indexValue) {
        this.indexValue = indexValue;
    }


    public void setInSweepAngle(float inSweepAngle) {
        this.inSweepAngle = inSweepAngle;
    }
}
