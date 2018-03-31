package com.ytempest.framelibrary.view.Button;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @author ytempest
 *         Description：
 */
public class VerifyButton extends ModifiableButton {

    private static String TAG = "VerifyButton";

    private boolean mIsStopCountDown = false;
    private boolean mIsInCountDown = false;

    private int mTimerCount;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mTimerCount--;
            if (mTimerCount > 0) {
                countDown();
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
        switchNormalStatus();
        if (mOnCountDownListener != null) {
            mOnCountDownListener.onFinish();
        }
    }

    private void countDown() {
        setText(getFormatDuring());
        if (!mIsStopCountDown) {
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    public void startCountDown(int count) {
        mTimerCount = count;
        mIsStopCountDown = false;
        switchDisableStatus();
        countDown();
    }

    public void startRequestCode() {
        switchRequestCodeStatus();
    }

    private void switchRequestCodeStatus() {
        switchDisableStatus();
        mIsInCountDown = true;
        setText("获取中...");
    }

    public String getFormatDuring() {
        String result = mTimerCount < 10 ? "0" + mTimerCount : "" + mTimerCount;
        result += "s后重获";
        return result;
    }

    public boolean isInCountDown() {
        return mIsInCountDown;
    }

    private OnCountDownListener mOnCountDownListener;

    public void setOnCountDownListener(OnCountDownListener onCountDownListener) {
        mOnCountDownListener = onCountDownListener;
    }

    public interface OnCountDownListener{
        void onFinish();
    }

}
