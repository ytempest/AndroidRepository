package com.ytempest.eventbusanalysis;

import android.content.Intent;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private TextView mTextView;
    private boolean isRegister = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tv_text);

        Log.e(TAG, "onCreate: start ---> " + SystemClock.currentThreadTimeMillis());
        EventBus.getDefault().register(this);
        Log.e(TAG, "onCreate: end ----> " + SystemClock.currentThreadTimeMillis());
    }


    public void onStartClick(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void onNormalClick(View view) {
        EventBus.getDefault().register(MainActivity.this);
        isRegister = true;
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void onStickClick(View view) {
        EventBus.getDefault().register(MainActivity.this);
        isRegister = true;
    }


    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100, sticky = true)
    public void onChangeText(String string) {
        Log.e(TAG, "对普通事件进行处理：" + string);
        mTextView.setText(string);
    }


    @Override
    protected void onDestroy() {
        if (isRegister) {
            EventBus.getDefault().unregister(this);
            isRegister = false;
        }
        super.onDestroy();
    }
}
