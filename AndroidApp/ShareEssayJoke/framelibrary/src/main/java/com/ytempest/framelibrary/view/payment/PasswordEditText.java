package com.ytempest.framelibrary.view.payment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.ytempest.framelibrary.R;

/**
 * @author ytempest
 *         Description：自定义密码输入框
 */
public class PasswordEditText extends EditText {
    /**
     * 密码框的边框大小
     */
    private float mFrameSize = 0.6f;

    /**
     * 密码框的边框颜色
     */
    private int mFrameColor = Color.parseColor("#A2A7AD");
    /**
     * 密码框的圆角弧度
     */
    private float mFrameCorner = 0;
    /**
     * 密码框分割线的大小
     */
    private float mDivisionSize = 0.5f;
    /**
     * 密码框分割线的颜色
     */
    private int mDivisionColor = Color.parseColor("#D3D3D3");
    /**
     * 密码圆点的半径
     */
    private float mPointRadius = 7.0f;
    /**
     * 密码圆点的颜色
     */
    private int mPointColor = Color.parseColor("#AAAAAA");
    /**
     * 密码的位数
     */
    private int mPasswordCount = 6;
    /**
     * 每一个密码占据的宽度，不包含边框和分割线
     */
    private float mPasswordItemWidth;
    /**
     * 默认的背景颜色
     */
    private int mDefaultBackground = Color.WHITE;
    /**
     * 密码输入完成的监听器
     */
    private PayView.OnInputFinishListener mOnInputFinishListener;
    private Paint mPaint;
    private Context mContext;

    public PasswordEditText(Context context) {
        this(context, null);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;

        initAttrs(context, attrs);

        initPaint();

        initConfig();

    }

    /**
     * 初始化密码框的一些配置
     */
    private void initConfig() {
        // 设置光标不可见，防止光标闪烁以不断调用onDraw方法重绘界面
        setCursorVisible(false);
        // 设置输入框不可用，防止点击输入框的时候调用系统的键盘
        setEnabled(false);
        // 默认只能够设置数字和字母
        setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        if (!(getBackground() instanceof ColorDrawable)) {
            // 如果EditText的背景是图片就不做更改
            if (getBackground() instanceof BitmapDrawable) {
                return;
            }
            // 如果EditText的背景不是颜色，就将背景更改
            setBackground(new ColorDrawable(mDefaultBackground));
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PasswordEditText);

        mFrameSize = attributes.getDimension(R.styleable.PasswordEditText_frameSize, dpToPx(mFrameSize));

        mFrameColor = attributes.getColor(R.styleable.PasswordEditText_frameColor, mFrameColor);

        mFrameCorner = attributes.getDimension(R.styleable.PasswordEditText_frameCorner, dpToPx(mFrameCorner));

        mDivisionSize = attributes.getDimension(R.styleable.PasswordEditText_divisionSize, dpToPx(mDivisionSize));

        mDivisionColor = attributes.getColor(R.styleable.PasswordEditText_divisionColor, mDivisionColor);

        mPointRadius = attributes.getDimension(R.styleable.PasswordEditText_pointRadius, dpToPx(mPointRadius));

        mPointColor = attributes.getColor(R.styleable.PasswordEditText_pointColor, mPointColor);

        mPasswordCount = attributes.getInt(R.styleable.PasswordEditText_passwordCount, mPasswordCount);

        attributes.recycle();
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, mContext.getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPasswordItemWidth = (getWidth() - 2 * mFrameSize - (mPasswordCount - 1) * mDivisionSize) / mPasswordCount;

        drawFrame(canvas);

        drawDivision(canvas);

        drawPassword(canvas);

        if (mOnInputFinishListener != null) {
            String currentPassword = getText().toString().trim();
            if (currentPassword.length() >= mPasswordCount) {
                mOnInputFinishListener.onFinish(currentPassword);
            }
        }
    }

    private void drawPassword(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPointColor);
        String password = getText().toString().trim();
        float cx;
        float cy = getHeight() / 2;
        for (int i = 0; i < password.length(); i++) {
            cx = mFrameSize + mPasswordItemWidth * (i + 0.5f) + i * mDivisionSize;
            canvas.drawCircle(cx, cy, mPointRadius, mPaint);
        }
    }

    /**
     * 绘制密码框的分割线
     */
    private void drawDivision(Canvas canvas) {
        mPaint.setStrokeWidth(mDivisionSize);
        mPaint.setColor(mDivisionColor);
        // 分割线X轴的起点位置
        float startX;
        // 分割线X轴的结束位置
        float endX;
        // 分割线Y轴的开始位置
        float startY = mFrameSize;
        // 分割线Y轴的结束位置
        float endY = getHeight() - mFrameSize;
        for (int i = 0; i < mPasswordCount - 1; i++) {
            startX = mFrameSize + (i + 1) * mPasswordItemWidth + i * mDivisionSize;
            endX = startX;
            canvas.drawLine(startX, startY, endX, endY, mPaint);
        }
    }

    /**
     * 绘制密码框
     */
    private void drawFrame(Canvas canvas) {
        RectF rectF = new RectF(mFrameSize, mFrameSize, getWidth() - mFrameSize, getHeight() - mFrameSize);
        // 画空心
        mPaint.setStyle(Paint.Style.STROKE);
        // 给画笔设置大小
        mPaint.setStrokeWidth(mFrameSize);
        mPaint.setColor(mFrameColor);
        if (mFrameCorner == 0) {
            canvas.drawRect(rectF, mPaint);
        } else {
            canvas.drawRoundRect(rectF, mFrameCorner, mFrameCorner, mPaint);
        }
    }

    /**
     * 向密码框输入密码
     *
     * @param number 一个数字
     */
    public void inputPassword(String number) {
        String currentPassword = getText().toString().trim();
        if (currentPassword.length() >= mPasswordCount) {
            return;
        }
        currentPassword += number;
        // 设置新密码，会重新绘制View
        setText(currentPassword);
    }

    /**
     * 删除最后一个密码
     */
    public void deleteLastPassword() {
        String password = getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            return;
        }
        String newPassword = password.substring(0, password.length() - 1);
        // 设置密码，会重新绘制View
        setText(newPassword);
    }


    public void setOnInputFinishListener(PayView.OnInputFinishListener listener) {
        this.mOnInputFinishListener = listener;
    }

}
