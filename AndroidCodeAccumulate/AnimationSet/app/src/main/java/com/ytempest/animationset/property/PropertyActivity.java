package com.ytempest.animationset.property;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ytempest.animationset.R;

public class PropertyActivity extends AppCompatActivity {

    private Button mIntValue;
    private Button mAlpha;
    private Button mTranslate;
    private Button mScale;
    private Button mRotate;
    private Button mSet;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);
        initView();

        initClick();

    }

    private void initView() {
        mIntValue = (Button) findViewById(R.id.bt_int_value);
        mAlpha = (Button) findViewById(R.id.bt_alpha);
        mTranslate = (Button) findViewById(R.id.bt_translate);
        mScale = (Button) findViewById(R.id.bt_scale);
        mRotate = (Button) findViewById(R.id.bt_rotate);
        mSet = (Button) findViewById(R.id.bt_anim_set);
        mImageView = (ImageView) findViewById(R.id.iv_image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PropertyActivity.this, "you click image !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initClick() {
        mIntValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PropertyActivity.this, IntValueActivity.class));
            }
        });

        mAlpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mImageView, "alpha", 1f, 0.5f, 0f);
                animator.setDuration(3000);
                animator.start();

                /*// 单个动画效果设置 & 参数设置
                mImageView.animate().alpha(0f).setDuration(2000).setInterpolator(new LinearInterpolator());
                // 组合动画:将按钮变成透明状态并同时移动到(300,300)处
                mImageView.animate().alpha(0f).x(300).y(300).setDuration(2000);*/


            }
        });

        mTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float curTranslationX = mImageView.getTranslationX();
                ObjectAnimator animator = ObjectAnimator.ofFloat(mImageView, "translationX", curTranslationX, 300);
                animator.setDuration(3000);
                animator.start();
            }
        });


        mScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mImageView, "scaleX", 1f, 2f, 0.5f);
                animator.setDuration(3000);
                animator.start();
            }
        });


        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mImageView, "rotationX", 0f, 360f);
                animator.setDuration(3000);
                animator.start();

            }
        });

        mSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float curTranslationX = mAlpha.getTranslationX();
                ObjectAnimator translation = ObjectAnimator.ofFloat(mImageView, "translationX", curTranslationX, 300);
                translation.setDuration(1000);

                ObjectAnimator rotate = ObjectAnimator.ofFloat(mImageView, "rotation", 0f, 360f);
                rotate.setDuration(2000);

                ObjectAnimator alpha = ObjectAnimator.ofFloat(mImageView, "alpha", 0f, 1f);
                alpha.setDuration(2000);

                // 步骤1：创建组合动画的对象
                AnimatorSet animSet = new AnimatorSet();

                // 步骤2：根据需求组合动画
                animSet.play(rotate).before(translation).with(alpha);

                animSet.start();


                // 创建组合动画对象  &  加载XML动画
                AnimatorSet animator = (AnimatorSet) AnimatorInflater.loadAnimator(PropertyActivity.this, R.animator.set_animator);
                // 设置动画作用对象
                animator.setTarget(mImageView);
                animator.start();
            }
        });

    }
}
