package com.ytempest.circleindicatorview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ytempest.circleindicatorview.view.CircleIndicatorView;

public class MainActivity extends AppCompatActivity {

    private CircleIndicatorView mCircleIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCircleIndicatorView = (CircleIndicatorView) findViewById(R.id.circle_index_view);

    }

    public void click(View view) {
        mCircleIndicatorView.goToPoint(77.88f);
    }
}
