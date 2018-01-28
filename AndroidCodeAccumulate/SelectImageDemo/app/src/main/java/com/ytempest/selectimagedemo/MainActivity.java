package com.ytempest.selectimagedemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import com.ytempest.selectimagedemo.imageSelect.ImageSelectActivity;
import com.ytempest.selectimagedemo.imageSelect.ImageSelector;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> mImages = new ArrayList<>();
    private int REQUEST_CODE = 0x11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void selectImage(View view) {
        selectPhoto();
    }

    public void selectPhoto() {
        ImageSelector.create().count(10).origin(mImages).showCamera(true).start(MainActivity.this, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                mImages = data.getStringArrayListExtra(ImageSelectActivity.EXTRA_RESULT);
            }
        }

    }
}
