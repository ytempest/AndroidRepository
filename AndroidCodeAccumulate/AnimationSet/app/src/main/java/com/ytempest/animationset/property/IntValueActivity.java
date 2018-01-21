package com.ytempest.animationset.property;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ytempest.animationset.R;

public class IntValueActivity extends AppCompatActivity {

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_int_value);

        mButton = (Button) findViewById(R.id.bt_ofint);

        final ViewWrapper viewWrapper = new ViewWrapper(mButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObjectAnimator.ofInt(viewWrapper, "width", 500).setDuration(1000).start();

                /*// 步骤1：设置属性数值的初始值 & 结束值
                // ValueAnimator.ofInt()内置了整型估值器,直接采用默认的.不需要设置
                ValueAnimator valueAnimator = ValueAnimator.ofInt(mButton.getLayoutParams().width, 600);

                // 步骤2：设置动画的播放各种属性
                valueAnimator.setDuration(2000);

                // 步骤3：设置更新监听器：即数值每次变化更新都会调用该方法
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        // 获得每次变化后的属性值
                        int currentValue = (Integer) animator.getAnimatedValue();

                        // 每次值变化时，将属性数值手动赋值给对象的属性
                        mButton.getLayoutParams().width = currentValue;

                        // 步骤4：刷新视图，即重新绘制，从而实现动画效果
                        mButton.requestLayout();
                        mButton.getWidth();

                    }
                });
                valueAnimator.start();*/
            }
        });
    }


}
