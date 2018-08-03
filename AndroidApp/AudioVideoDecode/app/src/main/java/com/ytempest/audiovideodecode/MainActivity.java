package com.ytempest.audiovideodecode;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ytempest.audiovideodecode.util.MediaUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final String mBasePath = Environment.getExternalStorageDirectory()
            + File.separator + "aaa" + File.separator + "decode" + File.separator;

    private final String mVideoPath = mBasePath + "input.mp4";

    private final String mTargetVideoPath = mBasePath + "output_640x480_yuv420p.yuv";

    private final String mAudioPath = mBasePath + "input.mp3";

    private final String mTargetAudioPath = mBasePath + "output_16_44100.pcm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void decodeVideoClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaUtils.decodeVideo(mVideoPath, mTargetVideoPath);
            }
        }).start();
    }

    public void decodeAudioClick(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaUtils.decodeAudio(mAudioPath, mTargetAudioPath);
            }
        }).start();
    }
}
