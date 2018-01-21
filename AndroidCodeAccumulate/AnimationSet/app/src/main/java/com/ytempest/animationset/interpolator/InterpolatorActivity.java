package com.ytempest.animationset.interpolator;

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.graphics.Interpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.ytempest.animationset.R;

public class InterpolatorActivity extends AppCompatActivity {

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interpolator);


        mButton = (Button) findViewById(R.id.bt_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获得当前按钮的位置
                float curTranslationY = mButton.getTranslationY();

                // 动画作用的对象的属性是Y轴平移
                // 动画效果是:从当前位置平移到 y=1450
                ObjectAnimator anim = ObjectAnimator.ofFloat(mButton, "translationY", curTranslationY, 1450);
                anim.setDuration(1000);
                // 设置插值器
                anim.setInterpolator(new DecelerateAccelerateInterpolator());
                // 启动动画
                anim.start();
            }
        });


    }


}
