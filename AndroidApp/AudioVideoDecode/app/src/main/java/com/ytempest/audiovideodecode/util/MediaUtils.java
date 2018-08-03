package com.ytempest.audiovideodecode.util;

/**
 * @author ytempest
 *         Description：
 */
public class MediaUtils {

    static{
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("postproc-54");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("media_decode");
    }

    public static native void decodeVideo(String input, String output);

    public static native void decodeAudio(String input, String output);
}
