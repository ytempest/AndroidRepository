package com.ytempest.animationset.evaluator;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ytempest.animationset.R;

public class EvaluatorActivity extends AppCompatActivity {

    private static final String TAG = "EvaluatorActivity";
    private Button mStart;
    private CircleView mCircleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluator);

        mStart = (Button) findViewById(R.id.bt_evaluator_start);
        mCircleView = (CircleView) findViewById(R.id.circle_view);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Point curPoint = new Point(mCircleView.currentPoint.getX(), mCircleView.currentPoint.getY());
                Point nextPoint = new Point(800, 1250);
                int curColor = mCircleView.getChangeColor();
                int nextColor = 0x23ff9f;

//                ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), curPoint, nextPoint);
//                anim.setDuration(1000);
//                mCircleView.startValueAnimation(anim);
                // 如果没有传入起始位置，就会调用setCurrentPoint() 方法获取 mCircleView 的位置
                // ObjectAnimator objectAnimator = ObjectAnimator.ofObject(mCircleView, "currentPoint", new PointEvaluator(), nextPoint);

                ObjectAnimator pointAnimator = ObjectAnimator.ofObject(mCircleView, "currentPoint", new PointEvaluator(), curPoint, nextPoint);
                pointAnimator.setDuration(3000);
                pointAnimator.start();
                ObjectAnimator colorAnimator = ObjectAnimator.ofObject(mCircleView, "changeColor", new ColorEvaluator(), curColor, nextColor);
                colorAnimator.setDuration(3000);
                colorAnimator.start();

            }
        });
    }
}
