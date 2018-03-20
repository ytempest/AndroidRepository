package com.ytempest.circleloadviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ytempest.widget.SplashLoadView;

public class MainActivity extends AppCompatActivity {

    private SplashLoadView mLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoad = findViewById(R.id.splash_load_view);
    }

    public void start(View view) {
        mLoad.startLoad();
    }

    public void stop(View view) {
        mLoad.stopLoading();
    }
}
