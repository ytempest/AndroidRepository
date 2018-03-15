package com.ytempest.baselibrary.view.indicator.item;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.ytempest.baselibrary.R;

/**
 * @author ytempest
 *         Description:
 */
public class ColorTrackTextView extends TextView {

    /**
     * 绘制初始颜色的画笔
     */
    private Paint mOriginPaint;
    /**
     * 绘制变色后颜色的画笔
     */
    private Paint mChangePaint;
    /**
     * 记录未变色和变色文字的中间位置
     */
    private float mCurrentProgress = 0f;
    private Direction mDirection = Direction.LEFT_TO_RIGHT;

    public enum Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }


    public ColorTrackTextView(Context context) {
        this(context, null);
    }

    public ColorTrackTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorTrackTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context, attrs);
    }

    /**
     * 初始化画笔
     *
     * @param attrs TextView 的属性集合
     */
    private void initPaint(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ColorTrackTextView);
        // 如果布局文件没有使用自定义属性的颜色就取默认颜色
        int originColor = array.getColor(R.styleable.ColorTrackTextView_originColor, getTextColors().getDefaultColor());
        int changeColor = array.getColor(R.styleable.ColorTrackTextView_originColor, getTextColors().getDefaultColor());
        // 初始化画笔
        mOriginPaint = getPaintByColor(originColor);
        mChangePaint = getPaintByColor(changeColor);

        // 一定要对该资源进行回收
        array.recycle();
    }

    /**
     * 重写该方法实现 TextView 的绘制
     *
     * @param canvas TextView 的画布
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        // 获取变色和未变色位置的确切值
        int middle = (int) (mCurrentProgress * width);
        String text = getText().toString();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        drawOriginText(canvas, text, middle);
        drawChangeText(canvas, text, middle);
    }

    /**
     * 绘制字体未变色的部分
     *
     * @param middle 未变色部分和变色部分的位置
     */
    private void drawOriginText(Canvas canvas, String text, int middle) {
        if (mDirection == Direction.LEFT_TO_RIGHT) {
            // 从 middle 到 getWidth() 一直绘制默认颜色（这个middle会逐渐变大）
            drawText(text, canvas, mOriginPaint, middle, getWidth());
        } else {
            // 从 0 到 getWidth()-middle 一直绘制默认颜色（这个 getWidth()-middle 会逐渐变小）
            drawText(text, canvas, mOriginPaint, 0, getWidth() - middle);

        }
    }

    /**
     * 绘制字体变色的部分
     *
     * @param middle 未变色部分和变色部分的位置
     */
    private void drawChangeText(Canvas canvas, String text, int middle) {
        if (mDirection == Direction.LEFT_TO_RIGHT) {
            // 从 0 到 middle 一直绘制变色的颜色（这个middle会逐渐变大）
            drawText(text, canvas, mChangePaint, 0, middle);
        } else {
            // 从 getWidth()-middle 到 getWidth() 一直绘制变色的颜色（这个 getWidth()-middle 会逐渐变小）
            drawText(text, canvas, mChangePaint, getWidth() - middle, getWidth());
        }
    }


    /**
     * 用相应的画笔对 text 从 start 绘制到 end
     *
     * @param paint 对文字进行绘制的画笔
     * @param start 绘制的开始位置
     * @param end   绘制的末端位置
     */
    public void drawText(String text, Canvas canvas, Paint paint, int start, int end) {
        // 保持画布状态
        canvas.save();
        // 截取要绘制的文字部分，后面进行颜色的绘制
        canvas.clipRect(new Rect(start, 0, end, getHeight()));
        // 获取字体的 bounds（left,top,right,bottom）,仅bottom为一个负数
        Rect rect = new Rect();
        // 获取截取后的文本大小、长度，然后保存在 rect 中
        mOriginPaint.getTextBounds(text, 0, text.length(), rect);
        // 获取字体的宽度
        int textWidth = rect.width();
        // x  就是代表绘制的开始部分  不考虑左右padding不相等的情况下 = 控件宽度的一半 - 字体宽度的一半
        int x = getWidth() / 2 - textWidth / 2;

        // 基线：文字都写在基线上面
        int baseLine = getBaseline();

        // 使用 paint 在画布上进行绘制
        canvas.drawText(text, x, baseLine, paint);

        // 释放画布
        canvas.restore();
    }

    /**
     * 根据传入的 color 创建一个 画笔
     */
    private Paint getPaintByColor(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        // 设置抗锯齿
        paint.setAntiAlias(true);
        // 设置防抖动
        paint.setDither(true);
        //设置画笔的大小，也就是 textview 的文字大小
        paint.setTextSize(getTextSize());

        return paint;
    }

    /**
     * 设置当前字体颜色绘制的进度
     */
    public void setCurrentProgress(float currentProgress) {
        // 设置刷新进度
        this.mCurrentProgress = currentProgress;
        // 重新绘制  会不断的调用onDraw方法
        invalidate();
    }

    /**
     * 设置绘制的方向，取值范围为{ LEFT_TO_RIGHT, RIGHT_TO_LEFT;}
     */
    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    public void setOriginColor(int color) {
        mOriginPaint.setColor(color);
    }

    public void setChangeColor(int color) {
        mChangePaint.setColor(color);
    }

    @Override
    public void setTextSize(float sp) {
        super.setTextSize(sp);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getResources().getDisplayMetrics());

        // 设置画笔大小
        mOriginPaint.setTextSize(px);
        mChangePaint.setTextSize(px);
    }
}
