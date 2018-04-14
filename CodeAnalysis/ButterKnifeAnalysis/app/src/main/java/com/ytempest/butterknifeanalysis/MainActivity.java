package com.ytempest.butterknifeanalysis;


import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {
/*
    @BindView(R.id.tv_one)
    TextView mTextView;*/

    private Unbinder mBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ButterKnife.bind(this);

    }

    @Override
    protected void onDestroy() {
        mBinder.unbind();
        super.onDestroy();
    }

    @OnClick(R.id.tv_one)
    void onOneClick(View view) {

    }

    @OnClick(R.id.tv_two)
    void onTwoClick(View view) {

    }

}
