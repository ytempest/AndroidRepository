package com.ytempest.voiceconvertor.util;

/**
 * @author ytempest
 *         Description：
 */
public class EffectUtils {
    // 正常声音
    public static final int MODE_NORMAL = 1;
    // 萝莉声
    public static final int MODE_LUOLI = 2;
    // 大叔声
    public static final int MODE_DASHU = 3;
    // 惊悚声
    public static final int MODE_JINGSONG = 4;
    // 搞怪声
    public static final int MODE_GAOGUAI = 5;
    // 空灵声
    public static final int MODE_KONGLING = 6;

    static {
        System.loadLibrary("fmod");
        System.loadLibrary("fmodL");
        System.loadLibrary("voice_convert");
    }

    public static native void convert(String audioPath, int type);
}
