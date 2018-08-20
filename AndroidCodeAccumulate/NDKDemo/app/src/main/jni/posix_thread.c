#include "com_ytempest_ndkdemo_util_PosixThread.h"
#include <stdio.h>
#include <stdlib.h>
#include <android/log.h>
#include <pthread.h>
#include <unistd.h>

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"posix_thread",CONTENT,__VA_ARGS__)

JavaVM *javaVM;

// 这个方法会在posix_thread动态库加载的时候调用，可以在这个方法中获取Java虚拟机
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("%s", "posix_thread动态库开始加载");
    javaVM = vm;
    return JNI_VERSION_1_6;
}


void *fun_th(void *args) {
    char *thread_no = (char *) args;

    JNIEnv *env = NULL;

    //通过JavaVM关联当前线程，获取当前线程的JNIEnv
    (*javaVM)->AttachCurrentThread(javaVM,&env, NULL);

    (*javaVM)->GetEnv(javaVM, (void **) &env, JNI_VERSION_1_6);

    jclass uuid_utils_cls = (*env)->FindClass(env, "com/ytempest/ndkdemo/util/UUIDUtils");
    uuid_utils_cls == NULL ? LOGI("%s", "uuid_utils_cls 为空") : LOGI("%s", "uuid_utils_cls 不为空");
    jmethodID get_uuid_mid = (*env)->GetStaticMethodID(env, uuid_utils_cls, "getUUID",
                                                       "()Ljava/lang/String;");
    int i = 0;
    for (i = 0; i < 4; i++) {
        jobject uuid_str = (*env)->CallStaticObjectMethod(env, uuid_utils_cls, get_uuid_mid);
        const char *uuid_cstr = (*env)->GetStringUTFChars(env, uuid_str, NULL);
        LOGI("线程%s：获取的UUID为：%s",thread_no,uuid_cstr);
        (*env)->ReleaseStringUTFChars(env, uuid_str, uuid_str);
        sleep(1);
    }



    //解除关联
    (*javaVM)->DetachCurrentThread(javaVM);

}

JNIEXPORT void JNICALL Java_com_ytempest_ndkdemo_util_PosixThread_thread
        (JNIEnv *env, jobject jobj) {

    pthread_t tid;
    pthread_create(&tid, NULL, fun_th, (void *) "No1");
    LOGI("%s", "进入方法");

    jclass uuid = (*env)->FindClass(env, "com/ytempest/ndkdemo/util/UUIDUtils");
    uuid == NULL ? LOGI("%s", "uuid 为空") : LOGI("%s", "uuid 不为空");

}