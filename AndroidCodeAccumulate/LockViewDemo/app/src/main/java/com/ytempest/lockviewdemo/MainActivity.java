package com.ytempest.lockviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ytempest.widget.LockView;

public class MainActivity extends AppCompatActivity {

    private LockView mLockView;
    private String mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLockView = findViewById(R.id.lock_view);
        mLockView.setOnLockPressListener(new LockView.OnLockPressListener() {
            @Override
            public boolean onFinish(String selectPoint) {
                if (mPassword == null) {
                    mPassword = selectPoint;
                    Toast.makeText(MainActivity.this, "请再次绘制", Toast.LENGTH_SHORT).show();
                    mLockView.restoreLockView();
                    return true;
                }
                if (mPassword.equals(selectPoint)) {
                    Toast.makeText(MainActivity.this, "密码是：" + selectPoint, Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(MainActivity.this, "两次绘制的结果不一样,请重新绘制", Toast.LENGTH_SHORT).show();

                    return false;
                }
            }

        });

    }

    public void onClick(View view) {
        mLockView.restoreLockView();
        mPassword = null;
    }
}
