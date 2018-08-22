#include "com_ytempest_ndkdemo_util_PosixThread.h"
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <pthread.h>
#include <unistd.h>

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"posix_thread",CONTENT,__VA_ARGS__)

JavaVM *javaVM;
// 全局引用，用于将在主线程中获取的Class传递到子线程中
jobject uuid_utils_cls_global;

// 这个方法会在posix_thread动态库加载的时候调用，可以在这个方法中获取Java虚拟机
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("%s", "posix_thread动态库开始加载");
    javaVM = vm;
    return JNI_VERSION_1_6;
}


JNIEXPORT void JNICALL Java_com_ytempest_ndkdemo_util_PosixThread_init
        (JNIEnv *env, jobject jobj) {
    // 如果要在子线程中使用某个类的Class，那么要先在主线程中获取，然后通过全局引用进行传递到子线程中
    // 1、获取clazz必须在主线程中
    jclass uuid_utils_cls = (*env)->FindClass(env, "com/ytempest/ndkdemo/util/UUIDUtils");
    // 2、初始化全局引用
    uuid_utils_cls_global = (*env)->NewGlobalRef(env, uuid_utils_cls);
}

void *fun_th(void *args) {
    // 获取线程名
    char *thread_no = (char *) args;

    //通过JavaVM关联当前线程，获取当前线程的JNIEnv
    JNIEnv *env = NULL;
    (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);

    // 获取UUIDUtils类的getUUID()方法id
    jmethodID get_uuid_mid = (*env)->GetStaticMethodID(env, uuid_utils_cls_global, "getUUID",
                                                       "()Ljava/lang/String;");
    int i = 0;
    for (i = 0; i < 7; i++) {
        jobject uuid_str = (*env)->CallStaticObjectMethod(env, uuid_utils_cls_global, get_uuid_mid);
        const char *uuid_cstr = (*env)->GetStringUTFChars(env, uuid_str, NULL);
        LOGI("线程%s：获取的UUID为：%s", thread_no, uuid_cstr);
        (*env)->ReleaseStringUTFChars(env, uuid_str, uuid_cstr);
        if (i == 5) {
            // 退出线程
            goto end;
        }
        sleep(1);
    }

    end:
    //解除关联
    (*javaVM)->DetachCurrentThread(javaVM);
    // 正常退出状态码为0
    pthread_exit((void *) 0);

}


void JNICALL Java_com_ytempest_ndkdemo_util_PosixThread_thread
        (JNIEnv *env, jobject jobj) {
    pthread_t tid;
    pthread_create(&tid, NULL, fun_th, (void *) "No1");
}


JNIEXPORT void JNICALL Java_com_ytempest_ndkdemo_util_PosixThread_destroy
        (JNIEnv *env, jobject jobj) {
    LOGI("%s", "posix_thread动态库开始释放资源");
    //释放全局引用
    (*env)->DeleteGlobalRef(env, uuid_utils_cls_global);
}