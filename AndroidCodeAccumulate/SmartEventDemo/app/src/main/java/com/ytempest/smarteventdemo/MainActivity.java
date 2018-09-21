package com.ytempest.smarteventdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ytempest.smartevent.SmartEvent;
import com.ytempest.smartevent.Subscribe;
import com.ytempest.smartevent.ThreadMode;
import com.ytempest.smartevent.meta.SimpleSubscriberInfo;
import com.ytempest.smartevent.meta.SubscriberMethodInfo;

import java.util.concurrent.Executors;

public class MainActivity extends SecondActivity {

    private static final String TAG = "BACKGROUNDActivity";
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.tv_msg);


        Log.e(TAG, "onCreate: start ---> " + SystemClock.currentThreadTimeMillis());
        SmartEvent.getDefault().register(this);
        Log.e(TAG, "onCreate: end ----> " + SystemClock.currentThreadTimeMillis());
    }




    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onTextChange(final Message msg) {
        if (msg.arg1 == 100) {
            String string = msg.obj.toString();
            Log.e(TAG, "onTextChange: msg1 --> " + string);

            mTextView.setText(string);


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onTextChange1(final Message msg) {
        if (msg.arg1 == 100) {
            String string = msg.obj.toString();
            Log.e(TAG, "onTextChange: msg1 --> " + string);

            mTextView.setText(string);


        }
    }


    public void onStartClick(View view) {
        startActivity(new Intent(MainActivity.this, SecondActivity.class));
    }

    @Override
    protected void onDestroy() {
        SmartEvent.getDefault().unregister(this);
        super.onDestroy();
    }


}
