package com.ytempest.skindemo;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.ytempest.skin.base.SkinCompatActivity;
import com.ytempest.skin.skin.SkinManager;
import com.ytempest.skin.skin.callback.OnSkinChangeListener;

import java.io.File;

public class MainActivity extends SkinCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mTextView;
    private String mSkinPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "night.skin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.tv_load);

        SkinManager.getInstance().addOnSkinChangeListener(new OnSkinChangeListener() {
            @Override
            public void onStart() {
                mTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(int errorCode) {
                Toast.makeText(MainActivity.this, "error: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSucceed() {
                mTextView.setVisibility(View.GONE);
            }
        });

    }

    public void change(View view) {
        SkinManager.getInstance().loadSkin(mSkinPath);
    }


    public void recover(View view) {
        SkinManager.getInstance().resetDefaultSkin();
    }
}
