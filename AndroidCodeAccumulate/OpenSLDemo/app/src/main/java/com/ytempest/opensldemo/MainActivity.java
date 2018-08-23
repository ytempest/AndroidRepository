package com.ytempest.opensldemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ytempest.opensldemo.util.AudioPlayer;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final String mAudioPath = Environment.getExternalStorageDirectory() + File.separator + "aaa"
            + File.separator + "test.wav";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onPlayClick(View view) {
        AudioPlayer.play(mAudioPath);
    }
}
