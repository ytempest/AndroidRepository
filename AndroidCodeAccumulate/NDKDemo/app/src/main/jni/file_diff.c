#define _CRT_SECURE_NO_WARNINGS

#include "com_ytempest_ndkdemo_util_DiffUtils.h"
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"file_diff.c",CONTENT,__VA_ARGS__)

long get_file_size(const char *file_path) {
    FILE *file_fp = fopen(file_path, "rb");
    fseek(file_fp, 0, SEEK_END);
    return ftell(file_fp);
}

void diff(const char *file_path, const char *file_pattern, int file_count) {
    // 创建分块后的文件地址
    // 获取一个char*指针的数组，数组每一个元素都是一个char指针，那么就需要用
    // 一个二级指针指向这个数组了每一个元素指向一个块文件地址
    char **patches = malloc(sizeof(char *) * file_count);

    // 为分块的文件地址分配内存，并进行赋值
    int i = 0;
    for (; i < file_count; i++) {
        // 为每一个块文件分配一块内存，用于存放块文件的物理位置
        patches[i] = malloc(sizeof(char) * 100);

        // 指定块文件的物理路径
        sprintf(patches[i], file_pattern, (i + 1));
    }

    // 获取源文件的大小
    long file_size = get_file_size(file_path);
    LOGI("源文件大小:%ld", file_size);

    // 获取每一个块的大小
    long patch_size = file_size / file_count;

    // 打开源文件
    FILE *file_fp = fopen(file_path, "rb");

    // 遍历所有的块文件，从源文件中读取其对应的数据块
    FILE *cur_patch;
    i = 0;
    long cursor = 0;
    long len;
    for (; i < file_count; i++) {
        // 打开当前要进行操作的块文件
        cur_patch = fopen(patches[i], "wb");

        // 不断从源文件中读取，然后将读取到的数据写到相应的块文件中
        char buffer[50];
        // seek已经读取的位置
        fseek(file_fp, cursor, SEEK_SET);
        while ((len = fread(buffer, sizeof(char), 50, file_fp)) != 0) {
            if (cursor < (patch_size * (i + 1)) || i == (file_count - 1)) {
                cursor = cursor + len * sizeof(char);
                fwrite(buffer, sizeof(char), len, cur_patch);
            } else {
                break;
            }
        }

        LOGI("第%d个文件块:%ld", (i + 1), cursor);
        // 关闭当前操作的块文件
        fclose(cur_patch);
    }

    // 关闭源文件
    fclose(file_fp);

    // 释放每一个用于保存分块文件物理地址的内存
    i = 0;
    for (; i < file_count; i++) {
        free(patches[i]);
    }

    // 释放用于保存所有分块文件指针的数组的内存
    free(patches);
}

void merge(const char *merge_path, const char *patches_pattern, int file_count) {
    // 申请一块char指针数组内存，用于存放指向块文件的char指针
    char **patches = malloc(sizeof(char *) * file_count);

    // 为每一个块文件申请一块内存地址用于存放块文件的物理地址
    int i = 0;
    for (; i < file_count; i++) {
        patches[i] = malloc(sizeof(char) * 100);

        sprintf(patches[i], patches_pattern, (i + 1));
    }

    // 打开合并文件
    FILE *merge_fp = fopen(merge_path, "wb");

    // 遍历所有的块文件，将其写到合并文件中
    FILE *cur_patch;
    i = 0;
    for (; i < file_count; i++) {
        // 打开当前要进行读取的块文件
        cur_patch = fopen(patches[i], "rb");

        // 判断是否存在这个文件
        if (cur_patch == NULL){
            continue;
        }

        char buffer[50];
        long len;
        while ((len = fread(buffer, sizeof(char), 50, cur_patch)) != 0) {
            fwrite(buffer, sizeof(char), len, merge_fp);
        }

        // 关闭已经读取完的块文件
        fclose(cur_patch);
    }

    // 关闭合并文件
    fclose(merge_fp);

    // 释放保存了分块文件物理地址的内存
    i = 0;
    for (; i < file_count; i++) {
        free(patches[i]);
    }
    // 释放保存了所有块文件地址的数组的内存
    free(patches);
}


/*
* Class:     com_ytempest_ndkdemo_util_DiffUtils
* Method:    diff
* Signature: (Ljava/lang/String;Ljava/lang/String;I)V
*/
JNIEXPORT void JNICALL Java_com_ytempest_ndkdemo_util_DiffUtils_diff
        (JNIEnv *env, jclass jcls, jstring file_path, jstring file_pattern, jint file_count) {
    // 获取文件和块文件的地址
    const char *file_path_c = (*env)->GetStringUTFChars(env, file_path, NULL);
    const char *file_pattern_c = (*env)->GetStringUTFChars(env, file_pattern, NULL);

    diff(file_path_c, file_pattern_c, file_count);

    // 释放内存
    (*env)->ReleaseStringUTFChars(env, file_path, file_path_c);
    (*env)->ReleaseStringUTFChars(env, file_pattern, file_pattern_c);
}

/*
* Class:     com_ytempest_ndkdemo_util_DiffUtils
* Method:    merge
* Signature: (Ljava/lang/String;Ljava/lang/String;I)V
*/
JNIEXPORT void JNICALL Java_com_ytempest_ndkdemo_util_DiffUtils_merge
        (JNIEnv *env, jclass cls, jstring merge_path, jstring file_pattern, jint file_count) {
    // 获取文件和块文件的地址
    const char *merge_path_c = (*env)->GetStringUTFChars(env, merge_path, NULL);
    const char *file_pattern_c = (*env)->GetStringUTFChars(env, file_pattern, NULL);

    merge(merge_path_c, file_pattern_c, file_count);

    // 释放内存
    (*env)->ReleaseStringUTFChars(env, merge_path, merge_path_c);
    (*env)->ReleaseStringUTFChars(env, file_pattern, file_pattern_c);
}