#define _CRT_SECURE_NO_WARNINGS

#include "com_ytempest_ndkdemo_util_EncryptUtils.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>


char password[15] = "iamytempest";

JNIEXPORT void JNICALL Java_com_ytempest_ndkdemo_util_EncryptUtils_encrypt
        (JNIEnv *env, jclass cls, jstring file_path, jstring encrypt_path) {
    // 将Java的字符串转换成C中的字符串
    const char *file_text = (*env)->GetStringUTFChars(env, file_path, NULL);
    const char *encrypt_text = (*env)->GetStringUTFChars(env, encrypt_path, NULL);

    // 打开文件
    FILE *file_fp = fopen(file_text, "rb");
    FILE *encrypt_fp = fopen(encrypt_text, "wb");

    // 获取密码长度
    int pwd_len = (int) strlen(password);

    // 每读取一个字符就跟密码进行异或处理，然后将异或后的字符写到加密文件中
    int ch;
    int i = 0;
    while ((ch = fgetc(file_fp)) != EOF) {
        ch = ch ^ password[i % pwd_len];
        fputc(ch, encrypt_fp);
        i++;
    }

    // 关闭文件
    fclose(file_fp);
    fclose(encrypt_fp);

    // 释放内存
    (*env)->ReleaseStringUTFChars(env, file_path, file_text);
    (*env)->ReleaseStringUTFChars(env, encrypt_path, encrypt_text);

}

JNIEXPORT void JNICALL Java_com_ytempest_ndkdemo_util_EncryptUtils_decrypt
        (JNIEnv *env, jclass cls, jstring encrypt_path, jstring decrypt_path) {
    const char *encrypt_text = (*env)->GetStringUTFChars(env, encrypt_path, NULL);
    const char *decrypt_text = (*env)->GetStringUTFChars(env, decrypt_path, NULL);

    // 打开文件
    FILE *encrypt_fp = fopen(encrypt_text, "rb");
    FILE *decrypt_fp = fopen(decrypt_text, "wb");

    // 获取密码长度
    int pwd_len = (int) strlen(password);

    // 读取要进行解密的文件，然后进行异或处理，最后写到目的文件中
    int ch;
    int i = 0;
    while ((ch = fgetc(encrypt_fp)) != EOF) {
        ch = ch ^ password[i % pwd_len];
        fputc(ch, decrypt_fp);
        i++;
    }

    // 关闭文件
    fclose(encrypt_fp);
    fclose(decrypt_fp);

    // 释放内存
    (*env)->ReleaseStringUTFChars(env, encrypt_path, encrypt_text);
    (*env)->ReleaseStringUTFChars(env, decrypt_path, decrypt_text);
}
