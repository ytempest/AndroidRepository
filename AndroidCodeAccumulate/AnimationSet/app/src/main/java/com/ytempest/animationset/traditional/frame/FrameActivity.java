package com.ytempest.animationset.traditional.frame;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ytempest.animationset.R;


public class FrameActivity extends AppCompatActivity {

    private ImageView mImageViewFilling;

    private ImageView mImageViewEmptying ;

    private ImageView mImageViewSelector ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        initView();
    }

    private void initView() {
        // 填充 left 的动画
        mImageViewFilling = (ImageView) findViewById(R.id.iv_animation_list_filling);
        AnimationDrawable fillingAnimation = (AnimationDrawable) mImageViewFilling.getDrawable();
        fillingAnimation.start();

        // 减少 left 的动画
        mImageViewEmptying = (ImageView) findViewById(R.id.iv_animation_list_emptying);
        AnimationDrawable emptyingAnimation = (AnimationDrawable) mImageViewEmptying.getBackground();
        emptyingAnimation.start();

        // 第一次点击是填充 left，第二次点击从填充好的状态减少 left
        mImageViewSelector = (ImageView) findViewById(R.id.iv_animated_selector);
        mImageViewSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageViewSelector.setActivated(!mImageViewSelector.isActivated());
            }
        });
    }



}
