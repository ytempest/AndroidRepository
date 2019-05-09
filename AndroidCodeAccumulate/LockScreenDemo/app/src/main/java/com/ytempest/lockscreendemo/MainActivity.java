package com.ytempest.lockscreendemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ytempest.lockscreendemo.lock.DeviceAdminTool;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onActivateClick(View view) {
        DeviceAdminTool.getInstance().activateLock();
    }

    public void onLockClick(View view) {
        DeviceAdminTool.getInstance().lockScreen();
    }
}
