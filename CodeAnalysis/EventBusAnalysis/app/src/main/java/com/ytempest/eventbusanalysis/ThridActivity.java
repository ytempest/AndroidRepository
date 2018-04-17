package com.ytempest.eventbusanalysis;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ThridActivity extends SecondActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        EventBus.getDefault().register(this);
    }



    @Subscribe(threadMode = ThreadMode.ASYNC, priority = 100, sticky = false)
    public void onAAAText(String string) {
    }



    @Subscribe(threadMode = ThreadMode.ASYNC, priority = 100, sticky = false)
    public void onBBBText(String string) {
    }


    @Subscribe(threadMode = ThreadMode.ASYNC, priority = 100, sticky = false)
    public void onCCCText(String string) {
    }


}
