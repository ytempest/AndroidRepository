package com.ytempest.loadviewdemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ytempest.loadviewdemo.ui.LoadView;

public class MainActivity extends AppCompatActivity {

    private View mLoadView;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mLoadView.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadView = findViewById(R.id.load_view);
        // 加载动画运行10秒后自动关闭
        mHandler.sendEmptyMessageDelayed(1, 10000);

    }
}
