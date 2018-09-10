package com.ytempest.selectimagedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ytempest.selectimagedemo.imageSelect.ImageSelectActivity;
import com.ytempest.selectimagedemo.imageSelect.ImageSelector;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private ArrayList<String> mImages = new ArrayList<>();
    private int REQUEST_CODE = 0x11;
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            10L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread thread = new Thread(r, "CompressImage");
                    thread.setDaemon(false);
                    return thread;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run: success");

            }
        });
    }

    public void selectImage(View view) {
        selectPhoto();

    }

    public void compressImage(View view) {


        // 把选择好的图片做了一下压缩
        for (final String path : mImages) {
            // 做优化  第一个decodeFile有可能会内存移除
            // 一般后台会规定尺寸  800  小米 规定了宽度 720
            // 上传的时候可能会多张 for循环 最好用线程池 （2-3）
            Log.e(TAG, "selectImage: path --> " + path);

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String destPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                            new File(path).getName();
                    Bitmap bitmap = ImageUtils.decodeFile(path);
                    ImageUtils.compressBitmap(bitmap, destPath, 85);
                }
            });


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
