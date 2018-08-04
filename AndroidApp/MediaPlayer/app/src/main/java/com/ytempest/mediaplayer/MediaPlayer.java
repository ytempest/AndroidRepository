package com.ytempest.mediaplayer;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;

/**
 * @author ytempest
 *         Description：
 */
public class MediaPlayer {

    static {
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("postproc-54");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("yuv");
        System.loadLibrary("media_play");
    }

    public native void playVideo(String videoPath, Surface surface);

    public native void playAudio(String audioPath);

    public AudioTrack createAudioTrack(int sampleRateInHz, int channelNumber) {
        // 音频码流的格式
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // 声道布局
        int channelConfig;
        if (channelNumber == 1) {
            channelConfig = AudioFormat.CHANNEL_IN_MONO;
        } else {
            // 默认为立体声
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        }

        // 缓存区大小（单位byte）
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

        // 播放
        // audioTrack.play();
        // 写入PCM
        // audioTrack.write(audioData, offsetInBytes, sizeInBytes);

        return new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRateInHz, channelConfig, audioFormat,
                bufferSizeInBytes, AudioTrack.MODE_STREAM);
    }
}