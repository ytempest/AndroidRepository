#include "com_ytempest_mediaplayer_MediaPlayer.h"
#include <stdlib.h>
#include <unistd.h>

#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswresample/swresample.h"

#include <android/log.h>

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"audio_player",CONTENT,##__VA_ARGS__)
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"audio_player",CONTENT,##__VA_ARGS__)

#define MAX_AUDIO_FRME_SIZE 4100*2

JNIEXPORT void JNICALL Java_com_ytempest_mediaplayer_MediaPlayer_playAudio
        (JNIEnv *env, jobject jobj, jstring audio_path) {

    const char *audio_cstr = (*env)->GetStringUTFChars(env, audio_path, NULL);

    // 注册所有组件
    av_register_all();

    // 获取封装格式的上下文
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    // 打开音频文件
    if (avformat_open_input(&pFormatCtx, audio_cstr, NULL, NULL) != 0) {
        LOGE("无法打开音频文件");
        goto free_format;
    }

    // 获取音频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("无法获取音频信息");
        goto free_format;
    }

    // 找到音频流在文件中的位置
    int audio_stream_index = -1;
    int i = 0;
    for (; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_stream_index = i;
            break;
        }
    }
    if (audio_stream_index == -1) {
        LOGE("找不到音频流");
        goto free_format;
    }

    // 获取解码器
    AVCodecContext *pCodecCtx = pFormatCtx->streams[audio_stream_index]->codec;
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
    if (pCodec == NULL) {
        LOGE("无法获取解码器");
        goto free_codec;
    }

    // 打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) != 0) {
        LOGE("无法打开解码器");
        goto free_codec;
    }

    // 初始化一些资源，如Packet、AVFrame
    AVPacket *packet = (AVPacket *) av_malloc(sizeof(AVPacket));
    AVFrame *pFrame = av_frame_alloc();

    // 设置重采样参数---------------start
    // 输入音频、输出音频的音频格式
    enum AVSampleFormat in_sample_fmt = pCodecCtx->sample_fmt;
    enum AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16;

    // 输入音频、输出音频的采样率
    int in_sample_rate = pCodecCtx->sample_rate;
    int out_sample_rate = 44100;

    // 输入音频、输出音频的声道布局
    uint64_t in_ch_layout = pCodecCtx->channel_layout;
    uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;

    // 初始化重采样上下文
    SwrContext *swr_context = swr_alloc();
    swr_alloc_set_opts(swr_context,
                       out_ch_layout, out_sample_fmt, out_sample_rate,
                       in_ch_layout, in_sample_fmt, in_sample_rate,
                       0, NULL);
    swr_init(swr_context);

    // 获取输出音频的声道数量
    int out_ch_number = av_get_channel_layout_nb_channels(out_ch_layout);
    // 设置重采样参数---------------end

    // 获取 AudioTrack对象
    jclass jcls = (*env)->GetObjectClass(env, jobj);
    jmethodID create_audio_track_mid = (*env)->GetMethodID(env, jcls, "createAudioTrack",
                                                           "(II)Landroid/media/AudioTrack;");
    jobject audio_track_obj = (*env)->CallObjectMethod(env, jobj, create_audio_track_mid,
                                                       out_sample_rate, out_ch_number);

    // 调用 AudioTrack.play方法
    jclass audio_track_cls = (*env)->GetObjectClass(env, audio_track_obj);
    jmethodID audio_track_play_mid = (*env)->GetMethodID(env, audio_track_cls, "play", "()V");
    (*env)->CallVoidMethod(env, audio_track_obj, audio_track_play_mid);

    // 获取 AudioTrack.write方法的ID
    jmethodID audio_track_write_mid = (*env)->GetMethodID(env, audio_track_cls, "write", "([BII)I");

    // 创建一块缓冲区，用于存放解码后的音频数据
    uint8_t *out_buffer = (uint8_t *) av_malloc(MAX_AUDIO_FRME_SIZE);

    // 不断遍历读取音频数据到packet中
    int got_frame, result;
    int frame_count = 0;

    while (av_read_frame(pFormatCtx, packet) >= 0) {
        // 如果当前的数据流是音频流就进行操作
        if (packet->stream_index == audio_stream_index) {
            frame_count++;

            // 将 packet中的音频数据解码到 AVFrame中
            result = avcodec_decode_audio4(pCodecCtx, pFrame, &got_frame, packet);
            LOGI("result->%d", result);

            // 解码异常
            if (result < 0) {
                LOGE("解码第%d帧出现异常", frame_count);
                av_packet_unref(packet);
                break;
            }

            // 解码正常
            if (got_frame > 0) {
                LOGI("解码到第%d帧", frame_count);

                // 将音频数据转为我们重采样格式的音频数据
                swr_convert(swr_context, &out_buffer, MAX_AUDIO_FRME_SIZE,
                            (const uint8_t **) pFrame->data, pFrame->nb_samples);

                // 获取采样的数据大小
                int out_buffer_size = av_samples_get_buffer_size(NULL, out_ch_number,
                                                                 pFrame->nb_samples, out_sample_fmt,
                                                                 1);

                // 将 out_buffer缓存求的数据 转成 byte数组----------start
                // 创建和out_buffer相同大小的byte数组
                jbyteArray jbyte_array = (*env)->NewByteArray(env, out_buffer_size);
                jbyte *jbyte_array_p = (*env)->GetByteArrayElements(env, jbyte_array, NULL);
                //out_buffer的数据复制到jbyte_array
                memcpy(jbyte_array_p, out_buffer, (size_t) out_buffer_size);

                // 将jni的数组数据同步到java层中
                (*env)->ReleaseByteArrayElements(env, jbyte_array, jbyte_array_p, 0);
                // 将 out_buffer缓存求的数据 转成 byte数组----------end

                // 调用 AudioTrack.write将数据写到 AudioTrack，然后就会播放了
                (*env)->CallIntMethod(env, audio_track_obj, audio_track_write_mid,
                                      jbyte_array, 0, out_buffer_size);

                // 释放byte数组局部引用，防止内存溢出
                (*env)->DeleteLocalRef(env, jbyte_array);
                usleep(1000 * 16);
            }
        }
        // 释放 packet资源
        av_packet_unref(packet);
    }

    (*env)->DeleteLocalRef(env, audio_track_obj);

    av_frame_free(&pFrame);
    av_free(out_buffer);

    swr_close(swr_context);
    swr_free(&swr_context);

    free_codec:
    avcodec_close(pCodecCtx);
    avcodec_free_context(&pCodecCtx);

    free_format:
    avformat_close_input(&pFormatCtx);
    avformat_free_context(pFormatCtx);
    (*env)->ReleaseStringUTFChars(env, audio_path, audio_cstr);
}