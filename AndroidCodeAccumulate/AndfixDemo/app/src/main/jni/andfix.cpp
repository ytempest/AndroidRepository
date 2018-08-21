#include "com_ytempest_andfixdemo_andfix_HandlerNative.h"
#include "dalvik.h"
#include <android/log.h>


#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"andfix",CONTENT,##__VA_ARGS__)
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"andfix",CONTENT,##__VA_ARGS__)

JNIEXPORT void JNICALL Java_com_ytempest_andfixdemo_andfix_HandlerNative_init
        (JNIEnv *env, jclass jcls, jint sdk_version) {
    // 打开libdvm.so，并获取其返回的句柄
    void *handle = dlopen("libdvm.so", RTLD_NOW);

    if (handle) {
        // 因为在sdk10以及之前使用的是dvmDecodeIndirectRef方法
        // sdk10之后使用的是_Z20dvmDecodeIndirectRefP6ThreadP8_jobject方法
        const char *name = sdk_version > 10 ? "_Z20dvmDecodeIndirectRefP6ThreadP8_jobject" :
                           "dvmDecodeIndirectRef";
        // 通过libdvm.so的句柄获取指定的方法
        dvmDecodeIndirectRef_fnPtr = (dvmDecodeIndirectRef_func) dlsym(handle, name);

        dvmThreadSelf_fnPtr = (dvmThreadSelf_func) dlsym(handle,
                                                         sdk_version > 10 ? "_Z13dvmThreadSelfv"
                                                                          : "dvmThreadSelf");

        // 反射获取Java的Method类的getDeclaringClass方法
        jclass clazz = env->FindClass("java/lang/reflect/Method");
        jClassMethod = env->GetMethodID(clazz, "getDeclaringClass", "()Ljava/lang/Class;");
    }
}


JNIEXPORT void JNICALL Java_com_ytempest_andfixdemo_andfix_HandlerNative_replaceMethod
        (JNIEnv *env, jclass jcls, jobject srcMethod, jobject destMethod) {
    LOGI("%s", "andfix：start replace method");

    jobject clazz = env->CallObjectMethod(destMethod, jClassMethod);
    ClassObject *clz = (ClassObject *) dvmDecodeIndirectRef_fnPtr(dvmThreadSelf_fnPtr(), clazz);
    // 重置
    clz->status = CLASS_INITIALIZED;

    Method *src = (Method *) env->FromReflectedMethod(srcMethod);
    Method *dest = (Method *) env->FromReflectedMethod(destMethod);
    src->clazz = dest->clazz;
    src->accessFlags = dest->accessFlags;
    src->methodIndex = dest->methodIndex;
    src->jniArgInfo = dest->jniArgInfo;
    src->registersSize = dest->registersSize;
    src->outsSize = dest->outsSize;
    src->insns = dest->insns;
    src->insSize = dest->insSize;
    src->nativeFunc = dest->nativeFunc;

    LOGI("%s", "andfix：finish replace method");
}

