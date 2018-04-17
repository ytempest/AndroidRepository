package com.ytempest.eventbusanalysis;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbusperf.MyEventBusIndex;

public class SecondActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        EventBus.getDefault().register(this);
    }


    public void onReturnClick(View view) {
        EventBus.getDefault().post("test");
        finish();
    }

    public void onReturnClickStick(View view) {
        EventBus.getDefault().postSticky("粘性");
        finish();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, priority = 100, sticky = false)
    public void onAppleText(String string) {
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, priority = 100, sticky = false)
    public void onBananaText(String string) {

    }


}
