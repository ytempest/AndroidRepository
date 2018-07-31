package com.ytempest.videoconvertor.util;

/**
 * @author ytempest
 *         Description：
 */
public class VideoUtils {

    static{
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("postproc-54");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("video_decode");
    }
    public static native void decode(String input, String output);
}
