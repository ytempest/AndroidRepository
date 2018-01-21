package com.ytempest.animationset.traditional.tween;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ytempest.animationset.R;

public class TweenActivity extends AppCompatActivity {

    private static final String TAG = "TweenActivity";
    private Button mAlpha;
    private Button mTranslate;
    private Button mScale;
    private Button mRotate;
    private Button mAnimSet;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tween);
        initView();
        initViewClick();

//        initXml();
    }

    private void initView() {
        mAlpha = (Button) findViewById(R.id.bt_alpha);
        mTranslate = (Button) findViewById(R.id.bt_translate);
        mScale = (Button) findViewById(R.id.bt_scale);
        mRotate = (Button) findViewById(R.id.bt_rotate);
        mAnimSet = (Button) findViewById(R.id.bt_anim_set);
        mImageView = (ImageView) findViewById(R.id.iv_tween_image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TweenActivity.this, "you click imageView", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initXml() {

        mAlpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1、创建 需要设置动画的 视图View
                ImageView mImageView = (ImageView) findViewById(R.id.iv_tween_image);
                // 2、创建 动画对象 并传入设置的动画效果xml文件
                AlphaAnimation alpha = (AlphaAnimation) AnimationUtils.loadAnimation(TweenActivity.this, R.anim.tween_alpha);
                // 3、播放动画
                mImageView.startAnimation(alpha);
            }
        });

        mTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView mImageView = (ImageView) findViewById(R.id.iv_tween_image);
                TranslateAnimation translate = (TranslateAnimation) AnimationUtils.loadAnimation(TweenActivity.this, R.anim.tween_translate);
                mImageView.startAnimation(translate);
            }
        });

        mScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView mImageView = (ImageView) findViewById(R.id.iv_tween_image);
                ScaleAnimation scale = (ScaleAnimation) AnimationUtils.loadAnimation(TweenActivity.this, R.anim.tween_scale);
                mImageView.startAnimation(scale);
            }
        });

        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView mImageView = (ImageView) findViewById(R.id.iv_tween_image);
                RotateAnimation rotate = (RotateAnimation) AnimationUtils.loadAnimation(TweenActivity.this, R.anim.tween_rotate);
                mImageView.startAnimation(rotate);
            }
        });

        mAnimSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView mImageView = (ImageView) findViewById(R.id.iv_tween_image);
                AnimationSet set = (AnimationSet) AnimationUtils.loadAnimation(TweenActivity.this, R.anim.tween_set);
                mImageView.startAnimation(set);
            }
        });
    }

    private void initViewClick() {
        mAlpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
                alpha.setInterpolator(new LinearInterpolator());
                alpha.setRepeatMode(Animation.REVERSE);
                alpha.setRepeatCount(1);
                alpha.setFillAfter(true);
                alpha.setDuration(2500);
                mImageView.startAnimation(alpha);
                mImageView.setEnabled(false);
            }
        });

        mTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TranslateAnimation translate = new TranslateAnimation(0.0f, 250f, 0.0f, 250f);
                translate.setRepeatMode(Animation.REVERSE);
                translate.setRepeatCount(3);
                translate.setDuration(2000);
                mImageView.startAnimation(translate);
            }
        });

        mScale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScaleAnimation scale = new ScaleAnimation(1.5f, 0.5f, 1.5f, 0.5f, mImageView.getWidth() / 2, mImageView.getHeight() / 2);
                scale.setDuration(2000);
                mImageView.startAnimation(scale);
            }
        });

        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RotateAnimation rotate = new RotateAnimation(0.0f, -360f, mImageView.getWidth() / 2, mImageView.getHeight() / 2);
                rotate.setRepeatMode(Animation.REVERSE);
                rotate.setRepeatCount(1);
                rotate.setDuration(2000);
                mImageView.startAnimation(rotate);
            }
        });

        mAnimSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationSet animationSet = new AnimationSet(TweenActivity.this, null);
                AlphaAnimation alphaStart = new AlphaAnimation(0.0f, 1.0f);
                alphaStart.setDuration(2000);
                ScaleAnimation scale = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, mImageView.getWidth() / 2, mImageView.getHeight() / 2);
                scale.setDuration(2000);
                RotateAnimation rotate = new RotateAnimation(0.0f, 3*360f, mImageView.getWidth() / 2, mImageView.getHeight() / 2);
                rotate.setDuration(2000);
                TranslateAnimation translate = new TranslateAnimation(0.0f, 200f, 0.0f, 0.0f);
                translate.setStartOffset(2000);
                translate.setDuration(1000);
                AlphaAnimation alphaEnd = new AlphaAnimation(1.0f, 0.0f);
                alphaEnd.setStartOffset(3000);
                alphaEnd.setDuration(1000);
                animationSet.addAnimation(alphaStart);
                animationSet.addAnimation(scale);
                animationSet.addAnimation(rotate);
                animationSet.addAnimation(translate);
                animationSet.addAnimation(alphaEnd);
                animationSet.setFillAfter(true);
                mImageView.startAnimation(animationSet);
            }
        });
    }

    @Override
    public void finish(){
        super.finish();
//        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        overridePendingTransition(R.anim.slide_exit_in,R.anim.slide_exit_out);
        // 采用overridePendingTransition(int enterAnim, int exitAnim)进行设置
        // enterAnim：从Activity a跳转到Activity b，进入b时的动画效果资源ID
        // exitAnim：从Activity a跳转到Activity b，离开a时的动画效果资源Id
        // 特别注意
        // overridePendingTransition()必须要在finish()后被调用才能生效
    }
}
