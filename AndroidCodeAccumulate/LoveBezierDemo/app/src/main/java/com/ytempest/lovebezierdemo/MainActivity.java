package com.ytempest.lovebezierdemo;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ytempest.widget.ThumbUpLayout;

public class MainActivity extends AppCompatActivity {

    private ThumbUpLayout mThumbUpLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mThumbUpLayout = findViewById(R.id.thump_up_layout);
    }

    public void onClick(View view) {
        mThumbUpLayout.onThumpUp();
    }
}
