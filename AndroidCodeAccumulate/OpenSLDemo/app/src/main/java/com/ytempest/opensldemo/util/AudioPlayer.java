package com.ytempest.opensldemo.util;

/**
 * @author ytempest
 *         Description：
 */
public class AudioPlayer {
    static{
        System.loadLibrary("audio_play");
    }
    public static native void play(String audioPath);
}
