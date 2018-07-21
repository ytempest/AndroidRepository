package com.ytempest.ndkdemo.util;

/**
 * @author ytempest
 *         Description：
 */
public class EncryptUtils {

    static{
        System.loadLibrary("file_crypt");
    }

    public static native void encrypt(String filePath, String encryptPath);

    public static native void decrypt(String encryptPath, String decryptPaht);

}
