package com.ytempest.framelibrary.view.button;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

/**
 * @author ytempest
 *         Description：实现了倒计时功能的Button
 */
public class VerifyButton extends ModifiableButton {

    private static final String TAG = "VerifyButton";

    private boolean mIsStopCountDown = false;
    private boolean mIsInCountDown = false;

    private int mTimerCount;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mTimerCount--;
            if (mTimerCount > 0) {
                countDown(mTimerCount);
            } else {
                stopCountDown();
            }
            return false;
        }
    });

    public VerifyButton(Context context) {
        this(context, null);
    }


    public VerifyButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerifyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    private void stopCountDown() {
        mIsStopCountDown = true;
        mIsInCountDown = false;
        // 切换按钮状态
        switchNormalStatus();
        if (mOnCountDownListener != null) {
            mOnCountDownListener.onFinish();
        }
    }

    private void countDown(int timerCount) {
        setText(getFormatDuring(timerCount));
        if (!mIsStopCountDown) {
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    private String getFormatDuring(int timerCount) {
        String result = timerCount < 10 ? "0" + timerCount : "" + timerCount;
        result += "s后重获";
        return result;
    }

    /**
     * 开始倒计时，从count一直到 0
     */
    public void startCountDown(int count) {
        mTimerCount = count;
        mIsStopCountDown = false;
        switchDisableStatus();
        countDown(count);
    }

    /**
     * 开始发送短信请求
     */
    public void startRequestCode() {
        switchRequestCodeStatus();
    }

    private void switchRequestCodeStatus() {
        switchDisableStatus();
        mIsInCountDown = true;
        setText("获取中...");
    }


    public boolean isInCountDown() {
        return mIsInCountDown;
    }

    private OnCountDownListener mOnCountDownListener;

    public void setOnCountDownListener(OnCountDownListener onCountDownListener) {
        mOnCountDownListener = onCountDownListener;
    }

    public interface OnCountDownListener {
        void onFinish();
    }

}
