#include "inc/fmod.hpp"
#include "com_ytempest_voiceconvertor_util_EffectUtils.h"
#include <unistd.h>

#include <android/log.h>

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"voice_convert",CONTENT,##__VA_ARGS__)
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"voice_convert",CONTENT,##__VA_ARGS__)

#define MODE_NORMAL 1       /* 原声 */
#define MODE_LUOLI 2        /* 萝莉声 */
#define MODE_DASHU 3        /* 大叔声 */
#define MODE_JINGSONG 4     /* 惊悚声 */
#define MODE_GAOGUAI  5     /* 搞怪声 */
#define MODE_KONGLING 6     /* 空灵声 */

using namespace FMOD;

JNIEXPORT void JNICALL Java_com_ytempest_voiceconvertor_util_EffectUtils_convert
        (JNIEnv *env, jclass jcls, jstring audio_path_str, jint type) {
    // 获取音频路径
    const char *audio_path_cstr = env->GetStringUTFChars(audio_path_str, NULL);

    // 定义 fmod的一些资源
    System *system = NULL;
    // 定义一个指向进行处理的声音资源的指针
    Sound *sound = NULL;
    // 音频通道
    Channel *channel = NULL;
    // 数字信号处理，用于对定义音效效果，然后添加到通道进行实现效果
    DSP *dsp;
    bool isplaying = true;

    try {
        // 初始化音频处理引擎
        System_Create(&system);
        // 默认初始化为32个音频通道，默认音调
        system->init(32, FMOD_INIT_NORMAL, NULL);

        // 创建声音，这里会从音频路径中读取声音，并保存在 sound的地址中
        system->createSound(audio_path_cstr, FMOD_DEFAULT, NULL, &sound);

        // 根据类型将转换声音音调
        switch (type) {
            case MODE_NORMAL :
                system->playSound(sound, 0, false, &channel);
                LOGI("convert to normal,  audio path:%s", audio_path_cstr);
                break;

            case MODE_LUOLI:
                // dsp -> 音效 创建fmod中预定义好的音效
                // FMOD_DSP_TYPE_PITCHSHIFT dsp，提升或者降低音调用的一种音效
                system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);

                //设置音调的参数
                dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 1.75F);

                // 这里应该会将音频放到一个通道上，以便后面通过这个通道增加一些特效
                system->playSound(sound, 0, false, &channel);

                //添加到channel，参数1表示要将这个dsp添加到哪一个轨道上面
                channel->addDSP(0, dsp);

                LOGI("convert to luoli,  audio path:%s", audio_path_cstr);
                break;

            case MODE_DASHU:
                system->createDSPByType(FMOD_DSP_TYPE_PITCHSHIFT, &dsp);
                dsp->setParameterFloat(FMOD_DSP_PITCHSHIFT_PITCH, 0.83F);

                system->playSound(sound, 0, false, &channel);

                channel->addDSP(0, dsp);
                LOGI("convert to dashu,  audio path:%s", audio_path_cstr);
                break;

            case MODE_JINGSONG:
                system->createDSPByType(FMOD_DSP_TYPE_TREMOLO, &dsp);
                dsp->setParameterFloat(FMOD_DSP_TREMOLO_SKEW, -0.9F);
                dsp->setParameterFloat(FMOD_DSP_TREMOLO_DEPTH, 0.7f);
                dsp->setParameterFloat(FMOD_DSP_TREMOLO_PHASE, 0.5f);

                system->playSound(sound, 0, false, &channel);


                channel->addDSP(0, dsp);
                LOGI("convert to jingsong,  audio path:%s", audio_path_cstr);
                break;

            case MODE_GAOGUAI:
                //提高说话的速度
                system->playSound(sound, 0, false, &channel);
                float frequency;
                channel->getFrequency(&frequency);
                frequency = frequency * 1.5f;
                channel->setFrequency(frequency);
                LOGI("convert to gaoguai,  audio path:%s", audio_path_cstr);
                break;

            case MODE_KONGLING:
                system->createDSPByType(FMOD_DSP_TYPE_ECHO, &dsp);
                dsp->setParameterFloat(FMOD_DSP_ECHO_DELAY, 150);
                dsp->setParameterFloat(FMOD_DSP_ECHO_WETLEVEL, -5);
                dsp->setParameterFloat(FMOD_DSP_ECHO_FEEDBACK, 10);

                system->playSound(sound, 0, false, &channel);

                channel->addDSP(0, dsp);
                LOGI("convert to kongling,  audio path:%s", audio_path_cstr);
                break;
            default:
                break;
        }

    } catch (...) {
        LOGE("there is an exception occurred during voice convert");
        // 释放内存
        env->ReleaseStringUTFChars(audio_path_str, audio_path_cstr);
        if (sound != NULL) {
            sound->release();
        }
        if (system != NULL) {
            system->close();
            system->release();
        }
        return;
        // 这里可以new一个Java的异常进行抛出
    }

    // 更新一下音效引擎，不然不会播放音频
    system->update();

    // 通过通道判断是否有音频正在播放
    while (isplaying) {
        channel->isPlaying(&isplaying);
        // 为什么要睡眠1秒
        // usleep(1000 * 1000);
    }

    // 释放内存
    sound->release();
    system->close();
    system->release();
    env->ReleaseStringUTFChars(audio_path_str, audio_path_cstr);
}