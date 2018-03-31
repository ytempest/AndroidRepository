package com.ytempest.selectimagedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


import com.ytempest.selectimagedemo.imageSelect.ImageSelectActivity;
import com.ytempest.selectimagedemo.imageSelect.ImageSelector;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
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

    public void compressImage(View view) {

        // 把选择好的图片做了一下压缩
        for (String path : mImages) {
            // 做优化  第一个decodeFile有可能会内存移除
            // 一般后台会规定尺寸  800  小米 规定了宽度 720
            // 上传的时候可能会多张 for循环 最好用线程池 （2-3）
            Log.e(TAG, "selectImage: path --> " + path);
            Bitmap bitmap = ImageUtil.decodeFile(path);
            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                    new File(path).getName();
            // 调用写好的native方法
            // 用Bitmap.compress压缩1/10
            ImageUtil.compressBitmap(bitmap, 75, fileName);
        }
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
