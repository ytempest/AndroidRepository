package com.ytempest.smarteventdemo;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.ytempest.smartevent.SmartEvent;
import com.ytempest.smartevent.Subscribe;
import com.ytempest.smartevent.ThreadMode;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SecondActivity extends AppCompatActivity {


    private static final String msg = "I'm ytempest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }


    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onReturnClick(final View view) {
        Message message = Message.obtain();
        message.arg1 = 100;
        message.obj = msg;
        SmartEvent.getDefault().post(message);
        finish();
    }

}
