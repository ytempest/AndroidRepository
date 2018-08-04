package com.ytempest.mediaplayer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ytempest.mediaplayer.view.VideoView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final String mMediaDir = Environment.getExternalStorageDirectory()
            + File.separator + "aaa" + File.separator + "play" + File.separator;

    private MediaPlayer mMediaPlayer;
    private VideoView mVideoView;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoView = findViewById(R.id.video_view);
        mMediaPlayer = new MediaPlayer();

        mSpinner = findViewById(R.id.spinner);
        String[] videoArray = getResources().getStringArray(R.array.video_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1, videoArray);
        mSpinner.setAdapter(adapter);
    }

    public void onVideoPlayClick(View view) {
        String video = mSpinner.getSelectedItem().toString();
        final String filePath = mMediaDir + video;
        final Surface surface = mVideoView.getHolder().getSurface();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMediaPlayer.playVideo(filePath, surface);
            }
        }).start();
    }

    public void onAudioPlayClick(View view) {
        final String audioPath = mMediaDir + "input.mp3";
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMediaPlayer.playAudio(audioPath);
            }
        }).start();
    }

}
