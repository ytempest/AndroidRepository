package com.ytempest.payviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ytempest.payviewdemo.ui.PasswordEditText;
import com.ytempest.payviewdemo.ui.PasswordKeyBoard;
import com.ytempest.payviewdemo.ui.PayView;

public class MainActivity extends AppCompatActivity {

    private PayView mPayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPayView = findViewById(R.id.pay_view);

        mPayView.setOnInputFinishListener(new PayView.OnInputFinishListener() {
            @Override
            public void onFinish(String password) {
                Toast.makeText(MainActivity.this, "password:" + password, Toast.LENGTH_SHORT).show();
            }
        });

        mPayView.setOnClosePayViewListener(new PayView.OnClosePayViewListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "you close the pay", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
