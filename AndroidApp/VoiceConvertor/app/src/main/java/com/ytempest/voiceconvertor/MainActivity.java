package com.ytempest.voiceconvertor;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.ytempest.voiceconvertor.util.EffectUtils;
import com.ytempest.voiceconvertor.util.FileUtils;

import org.fmod.FMOD;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int RECORD_AUDIO = 1;

    private String mAudioPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separatorChar + "aaa" + File.separatorChar + "voice.wav";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化 Fmod
        FMOD.init(this);
        setContentView(R.layout.activity_main);
    }

    public void onStartClick(View view) {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        startActivityForResult(intent, RECORD_AUDIO);
    }


    public void onNormalClick(View view) {
        new Thread() {
            @Override
            public void run() {
                EffectUtils.convert(mAudioPath, EffectUtils.MODE_NORMAL);
            }
        }.start();
    }

    public void onLuoLiClick(View view) {
        new Thread() {
            @Override
            public void run() {
                EffectUtils.convert(mAudioPath, EffectUtils.MODE_LUOLI);
            }
        }.start();
    }

    public void onDaShuClick(View view) {
        new Thread() {
            @Override
            public void run() {
                EffectUtils.convert(mAudioPath, EffectUtils.MODE_DASHU);
            }
        }.start();
    }

    public void onJingSongClick(View view) {
        new Thread() {
            @Override
            public void run() {
                EffectUtils.convert(mAudioPath, EffectUtils.MODE_JINGSONG);
            }
        }.start();
    }

    public void onGaoGuaiClick(View view) {
        new Thread() {
            @Override
            public void run() {
                EffectUtils.convert(mAudioPath, EffectUtils.MODE_GAOGUAI);
            }
        }.start();
    }

    public void onKongLingClick(View view) {
        new Thread() {
            @Override
            public void run() {
                EffectUtils.convert(mAudioPath, EffectUtils.MODE_KONGLING);
            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RECORD_AUDIO:
                try {
                    Uri uri = data.getData();
                    File file = new File(getAudioFilePathFromUri(uri));
                    File audioFile = new File(mAudioPath);
                    FileUtils.copyFile(file, audioFile);
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;

            default:
                break;
        }
    }

    private String getAudioFilePathFromUri(Uri uri) {
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
        return cursor.getString(index);
    }

    @Override
    protected void onDestroy() {
        FMOD.close();
        super.onDestroy();
    }
}
