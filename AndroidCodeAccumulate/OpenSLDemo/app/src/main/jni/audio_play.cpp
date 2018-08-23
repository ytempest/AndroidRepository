#include "com_ytempest_opensldemo_util_AudioPlayer.h"

extern "C" {
#include "wavlib.h"
}

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <android/log.h>
#include "CreateBufferQueueAudioPlayer.cpp"

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"audio_play",CONTENT,##__VA_ARGS__);
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"audio_play",CONTENT,##__VA_ARGS__);

#define ARRAY_LEN(a) (sizeof(a)/sizeof(a[0]))

// wav文件指针
WAV wav;
// 引擎对象
SLObjectItf engineObject;
// 引擎接口
SLEngineItf engineInterface;
// 混音器
SLObjectItf outputMixObject;
// 播放器对象
SLObjectItf audioPlayerObject;
// 缓冲器队列接口
SLAndroidSimpleBufferQueueItf androidSimpleBufferQueueItf;
// 播放接口
SLPlayItf audioPlayInterface;


// 缓冲区
unsigned char *buffer;
// 缓冲区大小
size_t bufferSize;


// 上下文
struct PlayerContext {
    WAV wav;
    unsigned char *buffer;
    size_t bufferSize;

    PlayerContext(WAV wav, unsigned char *buffer, size_t bufferSize) {
        this->wav = wav;
        this->buffer = buffer;
        this->bufferSize = bufferSize;
    }
};

// 打开文件
WAV OpenWaveFile(JNIEnv *env, jstring jWavFile) {
    const char *cWavFile = env->GetStringUTFChars(jWavFile, NULL);
    WAVError err;
    WAV wav = wav_open(cWavFile, WAV_READ, &err);

    LOGI("%d", wav_get_bitrate(wav));
    env->ReleaseStringUTFChars(jWavFile, cWavFile);

    if (wav == 0) {
        LOGE("%s", wav_strerror(err));
    }
    return wav;
}

// 关闭文件
void CloseWaveFile(WAV wav) {
    wav_close(wav);
}

// 实现对象
void RealizeObject(SLObjectItf object) {
    // 非异步（阻塞）
    (*object)->Realize(object, SL_BOOLEAN_FALSE);
}

// 回调函数
void PlayerCallBack(SLAndroidSimpleBufferQueueItf androidSimpleBufferQueueItf, void *context) {
    PlayerContext *ctx = (PlayerContext *) context;

    // 读取数据
    ssize_t readSize = wav_read_data(ctx->wav, ctx->buffer, ctx->bufferSize);
    if (readSize > 0) {
        (*androidSimpleBufferQueueItf)->Enqueue(androidSimpleBufferQueueItf, ctx->buffer,
                                                (SLuint32) readSize);
    } else {
        // destroy context
        CloseWaveFile(ctx->wav);    // 关闭文件
        delete ctx->buffer;     // 释放缓存
    }
}


JNIEXPORT void JNICALL Java_com_ytempest_opensldemo_util_AudioPlayer_play
        (JNIEnv *env, jclass jcls, jstring audio_path_str) {

    // 1、打开文件
    WAV wav = OpenWaveFile(env, audio_path_str);

    // 2、创建OpenSL ES引擎
    // OpenSL ES在Android平台下默认是线程安全的，这样设置是为了兼容其他平台
    SLEngineOption options[] = {
            (SLuint32) SL_ENGINEOPTION_THREADSAFE, (SLuint32) SL_BOOLEAN_TRUE
    };
    slCreateEngine(&engineObject, ARRAY_LEN(engineObject), options, 0, 0, 0);    // 没有接口

    // 实例化对象
    // 对象创建之后，处于位实例化状态，对象虽然存在当未分配任何资源，使用前先实例化（使用完destroy）
    RealizeObject(engineObject);

    // 3、获取引擎接口
    (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineInterface);

    // 4、创建输出混音器
    (*engineInterface)->CreateOutputMix(engineInterface, &outputMixObject, 0, 0, 0);    //没有接口

    // 实例化混音器
    RealizeObject(outputMixObject);

    // 5、创建缓冲区保存读取到的音频数据库
    // 缓冲区大小
    bufferSize = wav_get_channels(wav) * wav_get_rate(wav) * wav_get_bits(wav);
    buffer = new unsigned char[bufferSize];

    // 6、创建带有缓冲区队列的音频播放器
    CreateBufferQueueAudioPlayer(wav, engineInterface, outputMixObject, audioPlayerObject);

    // 实例化音频播放器
    RealizeObject(audioPlayerObject);

    // 7、获取缓冲区队列接口 Buffer Queue Interface
    // 通过缓冲区队列接口对缓冲区进行排序播放
    (*audioPlayerObject)->GetInterface(audioPlayerObject, SL_IID_BUFFERQUEUE,
                                       &androidSimpleBufferQueueItf);

    // 8、注册音频播放器回调函数
    // 当缓冲区完成对前一个缓冲区队列的播放时，回调函数就会被调用，然后我们又继续读取音频数据，直到结束
    // 上下文，包裹参数方便在回调函数中使用
    PlayerContext *ctx = new PlayerContext(wav, buffer, bufferSize);
    (*androidSimpleBufferQueueItf)->RegisterCallback(androidSimpleBufferQueueItf, PlayerCallBack,
                                                     ctx);

    // 9、获取Play Interface通过对SetPlayState函数来启动播放音乐
    // 一旦播放器被设置成播放状态，该音频播放器开始等待缓冲区排队就绪
    (*audioPlayerObject)->GetInterface(audioPlayerObject, SL_IID_PLAY, &audioPlayInterface);
    // 设置播放状态
    (*audioPlayInterface)->SetPlayState(audioPlayInterface, SL_PLAYSTATE_PLAYING);

    // 10、开始，让第一个缓冲区入队
    PlayerCallBack(androidSimpleBufferQueueItf, ctx);

    // 关闭文件
    // CloseWaveFile(wav);
}

