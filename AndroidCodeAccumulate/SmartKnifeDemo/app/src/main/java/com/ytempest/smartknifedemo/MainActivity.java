package com.ytempest.smartknifedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ytempest.smartknife.SmartKnife;
import com.ytempest.smartknife.UnBinder;
import com.ytempest.smartknife_annotations.LinkClick;
import com.ytempest.smartknife_annotations.LinkView;


public class MainActivity extends AppCompatActivity {

    @LinkView(R.id.tv_one)
    public TextView mTextViewOne;

    @LinkView(R.id.tv_two)
    TextView mTextViewTwo;
    private UnBinder mBind;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBind = SmartKnife.bind(this);
        mTextViewOne.setText("mTextViewOne");

        mTextViewTwo.setText("mTextViewTwo");

    }

    @LinkClick(R.id.tv_one)
    void onTextClick(View view) {
        Toast.makeText(this, "you click one", Toast.LENGTH_SHORT).show();

    }

    @LinkClick(R.id.tv_two)
    void onTTTTClick(View view) {
        Toast.makeText(this, "you click two", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        mBind.unbind();
        super.onDestroy();
    }
}
