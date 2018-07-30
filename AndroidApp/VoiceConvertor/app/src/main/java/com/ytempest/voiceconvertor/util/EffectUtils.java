package com.ytempest.voiceconvertor.util;

/**
 * @author ytempest
 *         Description：
 */
public class EffectUtils {
    public static final int MODE_NORMAL = 1;
    public static final int MODE_LUOLI = 2;
    public static final int MODE_DASHU= 3;
    public static final int MODE_JINGSONG= 4;
    public static final int MODE_GAOGUAI = 5;
    public static final int MODE_KONGLING= 6;

    static{
        System.loadLibrary("fmod");
        System.loadLibrary("fmodL");
        System.loadLibrary("voice_convert");

    }

    public static native void convert(String audioPath, int type);
}
