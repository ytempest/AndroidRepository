package com.ytempest.videoconvertor;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ytempest.videoconvertor.util.VideoUtils;

import java.io.File;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String mSourcePath = Environment.getExternalStorageDirectory()
            + File.separator + "aaa" + File.separator + "input.mp4";

    private static final String mTargetPath = Environment.getExternalStorageDirectory()
            + File.separator + "aaa" + File.separator + "output_640x480_yuv420p.yuv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void decodeClick(View view) {
        VideoUtils.decode(mSourcePath, mTargetPath);
    }
}
