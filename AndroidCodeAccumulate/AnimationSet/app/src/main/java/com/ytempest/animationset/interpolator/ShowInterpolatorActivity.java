package com.ytempest.animationset.interpolator;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.ytempest.animationset.MainActivity;
import com.ytempest.animationset.R;

public class ShowInterpolatorActivity extends AppCompatActivity {

    private Button mStart;
    private Button mOne;
    private Button mTwo;
    private Button mThree;
    private Button mFour;
    private Button mFive;
    private Button mSix;
    private Button mSeven;
    private Button mEight;
    private Button mNine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showinterpolator);
        mStart = (Button) findViewById(R.id.start);
        mOne = (Button) findViewById(R.id.one);
        mTwo = (Button) findViewById(R.id.two);
        mThree = (Button) findViewById(R.id.three);
        mFour = (Button) findViewById(R.id.four);
        mFive = (Button) findViewById(R.id.five);
        mSix = (Button) findViewById(R.id.six);
        mSeven = (Button) findViewById(R.id.seven);
        mEight = (Button) findViewById(R.id.eight);
        mNine = (Button) findViewById(R.id.nine);

        initView();
    }

    private void initView() {
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInterpolator(mOne, new AccelerateInterpolator());
                startInterpolator(mTwo, new OvershootInterpolator());
                startInterpolator(mThree, new AccelerateDecelerateInterpolator());
                startInterpolator(mFour, new AnticipateInterpolator());
                startInterpolator(mFive, new AnticipateOvershootInterpolator());
                startInterpolator(mSix, new BounceInterpolator());
                startInterpolator(mSeven, new CycleInterpolator(1.0f));
                startInterpolator(mEight, new DecelerateInterpolator());
                startInterpolator(mNine, new LinearInterpolator());
            }

        });
    }

    private void startInterpolator(TextView textView, Interpolator interpolator) {
        float curTranslationY = textView.getTranslationY();

        // 创建动画对象 & 设置动画
        // 表示的是:
        // 动画作用对象是mButton
        // 动画作用的对象的属性是X轴平移
        // 动画效果是:从当前位置平移到 y=1500 再平移到初始位置
        ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "translationY", curTranslationY, 1050);

        animator.setDuration(2500);
        // 设置插值器
        animator.setInterpolator(interpolator);
        // 启动动画
        animator.start();
    }
}
