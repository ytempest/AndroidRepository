package com.ytempest.animationset;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ytempest.animationset.evaluator.EvaluatorActivity;
import com.ytempest.animationset.interpolator.InterpolatorActivity;
import com.ytempest.animationset.interpolator.ShowInterpolatorActivity;
import com.ytempest.animationset.property.PropertyActivity;
import com.ytempest.animationset.traditional.frame.FrameActivity;
import com.ytempest.animationset.traditional.tween.TweenActivity;

/**
 * @author ytempest
 */
public class MainActivity extends AppCompatActivity {
    private Button mFrame;
    private Button mTween;
    private Button mInterpolator;
    private Button mShowInterpolator;
    private Button mEvaluator;
    private Button mProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        initClick();

    }

    private void initView() {
        mFrame = (Button) findViewById(R.id.bt_frame);
        mTween = (Button) findViewById(R.id.bt_tween);
        mInterpolator = (Button) findViewById(R.id.bt_interpolator);
        mShowInterpolator = (Button) findViewById(R.id.bt_show_interpolator);
        mEvaluator = (Button) findViewById(R.id.bt_evaluator);
        mProperty = (Button) findViewById(R.id.bt_property);
    }

    private void initClick() {
        mFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FrameActivity.class));

            }

        });

        mTween.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TweenActivity.class));
                //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                overridePendingTransition(R.anim.slide_start_in, R.anim.slide_start_out);
            }
        });

        mInterpolator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, InterpolatorActivity.class));
            }
        });

        mShowInterpolator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShowInterpolatorActivity.class));
            }
        });
        mEvaluator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EvaluatorActivity.class));
            }
        });
        mProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PropertyActivity.class));
            }
        });
    }
}
