package com.ytempest.recycleranalysis.ListIndicator;

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

import com.ytempest.recycleranalysis.R;

/**
 * @author ytempest
 *         Description：索引指示器，可用于 RecyclerView作指示用
 */
public class ListIndicator extends View {

    private static String[] mIndexList = new String[]{"#", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    private int mTextColor = Color.GRAY;

    private int mChangeColor = Color.BLUE;

    private int mTextSize = spToPx(15);

    private Paint mDefaultPaint;

    private Paint mChangePaint;

    private boolean mIsTouchItem;
    /**
     * 一个条目的高度
     */
    private float mSingleHeight;
    /**
     * 当前正触摸的条目
     */
    private String mCurrentTouchItem;
    /**
     * 指示器触摸时的监听器
     */
    private OnIndexTouchListener mOnIndexTouchListener;


    public ListIndicator(Context context) {
        this(context, null);
    }

    public ListIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);

        initPaint();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ListIndicator);

        mTextColor = attributes.getColor(R.styleable.ListIndicator_textColor, mTextColor);

        mChangeColor = attributes.getColor(R.styleable.ListIndicator_selectColor, mChangeColor);

        mTextSize = attributes.getDimensionPixelSize(R.styleable.ListIndicator_textSize, mTextSize);

        attributes.recycle();

    }

    private void initPaint() {
        mDefaultPaint = getPaint(mTextColor);
        mChangePaint = getPaint(mChangeColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 定义绘制文字的宽度
        int textWidth = (int) mDefaultPaint.measureText("AA");
        // 获取指示器的宽度
        int width = textWidth + getPaddingLeft() + getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
        // 获取指示器中的内容的每一个条目的高度
        mSingleHeight = (getHeight() - getPaddingTop() - getPaddingBottom()) / mIndexList.length;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mIndexList.length; i++) {
            String item = mIndexList[i];
            float textWidth = (int) mDefaultPaint.measureText(item);
            float contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            float x = getPaddingLeft() + (contentWidth - textWidth) / 2;
            Paint.FontMetrics fontMetrics = mDefaultPaint.getFontMetrics();
            // 文字高度的一半
            float textCenterY = (Math.abs(fontMetrics.top) - fontMetrics.bottom) / 2;
            // 基线 = topPadding + 上一个条目位置 + 条目高度的一半 + 文字高度的一半
            float y = getPaddingTop() + mSingleHeight / 2 + (mSingleHeight * i) + textCenterY;
            // 如果触摸的是当前位置则用画笔重画
            if (mIndexList[i].equals(mCurrentTouchItem)) {
                canvas.drawText(item, x, y, mChangePaint);
                continue;
            }
            canvas.drawText(item, x, y, mDefaultPaint);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float fingerY = event.getY();
                // 如果触摸的位置不在条目中（即在paddingTop或paddingBottom中）则 return
                if (fingerY < getPaddingTop() || fingerY > getHeight() + getPaddingTop()) {
                    return true;
                }
                // 获取当前触摸对应的条目位置
                int position = Math.round((fingerY - getPaddingTop()) / mSingleHeight);

                if (position < 0) {
                    position = 0;
                }

                if (position > mIndexList.length - 1) {
                    position = mIndexList.length - 1;
                }

                // 如果重复触摸同一条目就return
                if (mIndexList[position].equals(mCurrentTouchItem)) {
                    return true;
                }

                mIsTouchItem = true;
                if (mOnIndexTouchListener != null) {
                    mOnIndexTouchListener.onTouch(mIndexList[position], true);
                }
                // 记录当前触摸的条目
                mCurrentTouchItem = mIndexList[position];
                break;

            case MotionEvent.ACTION_UP:
                mIsTouchItem = false;
                if (mOnIndexTouchListener != null) {
                    mOnIndexTouchListener.onTouch(mCurrentTouchItem, false);
                }
                break;
            default:
                break;

        }
        // 重新绘制界面
        invalidate();
        return true;
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    public Paint getPaint(int color) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(mTextSize);
        paint.setColor(color);
        return paint;
    }


    public void setOnIndexTouchListener(OnIndexTouchListener listener) {
        this.mOnIndexTouchListener = listener;
    }

    public interface OnIndexTouchListener {
        /**
         * 当索引指示器被触摸的时候会被调用
         *
         * @param item        触摸位置的条目
         * @param isTouchItem 是否正在触摸
         */
        void onTouch(String item, boolean isTouchItem);
    }

    /**
     * 设置索引条目
     */
    public void setIndexList(String[] strings) {
        mIndexList = strings;
    }

    public boolean isTouchItem() {
        return mIsTouchItem;
    }
}
