package com.ytempest.recycleranalysis.ListIndicator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ytempest.recycleranalysis.R;

public class ListIndicatorActivity extends AppCompatActivity {

    private ListIndicator mListIndicator;
    private TextView mCenterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_indicator);

        mListIndicator = findViewById(R.id.list_indicator);
        mCenterText = findViewById(R.id.tv_center);

        mListIndicator.setOnIndexTouchListener(new ListIndicator.OnIndexTouchListener() {
            @Override
            public void onTouch(String item, boolean isTouchItem) {
                if (isTouchItem) {
                    mCenterText.setVisibility(View.VISIBLE);
                    mCenterText.setText(item);
                } else {
                    mCenterText.setVisibility(View.GONE);
                }
            }
        });
    }
}
