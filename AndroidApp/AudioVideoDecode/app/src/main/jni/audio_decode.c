#include "com_ytempest_audiovideodecode_util_MediaUtils.h"

// 封装格式
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswresample/swresample.h"

#include <time.h>
#include <android/log.h>

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO,"audio_decode",CONTENT,##__VA_ARGS__)
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"audio_decode",CONTENT,##__VA_ARGS__)

// 缓存区大小
#define MAX_AUDIO_FRME_SIZE 44100 * 2

JNIEXPORT void JNICALL Java_com_ytempest_audiovideodecode_util_MediaUtils_decodeAudio
        (JNIEnv *env, jclass jcls, jstring input_path, jstring output_path) {

    const char *input_cstr = (*env)->GetStringUTFChars(env, input_path, NULL);
    const char *output_cstr = (*env)->GetStringUTFChars(env, output_path, NULL);

    // 注册组件
    av_register_all();

    // 获取封装的上下文
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    // 打开音频文件
    if (avformat_open_input(&pFormatCtx, input_cstr, NULL, NULL) != 0) {
        LOGE("无法打开音频文件");
        goto free_format;
    }

    // 获取音频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("无法获取音频信息");
        goto free_format;
    }

    // 获取音频流的索引位置
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
        LOGE("找不到音频的解码器");
        goto free_codec;
    }

    // 打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) != 0) {
        LOGE("无法打开解码器");
        goto free_codec;
    }

    // 初始化一些资源
    // 初始化 packet，用于存放一帧数据
    AVPacket *packet = (AVPacket *) av_malloc(sizeof(AVPacket));

    // 初始化 AVFrame，用于存放流数据
    AVFrame *frame = av_frame_alloc();

    // 获取采样的上下文
    // frame->16bit 44100 PCM 统一音频采样格式与采样率
    SwrContext *swr_context = swr_alloc();

    // 重采样设置参数-------------------start
    // 输入采样格式
    enum AVSampleFormat in_sample_fmt = pCodecCtx->sample_fmt;

    // 输出采样格式 16bit PCM
    enum AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16;

    // 输入的采样率
    int in_sample_rate = pCodecCtx->sample_rate;

    // 输出的采样率
    int out_sample_rate = 44100;

    // 输入的声道布局
    // 方法之一：根据声道的个数获取默认的声道布局（2个声道默认为立体声）
    // av_get_default_channel_layout(pCodecCtx->channels);
    uint64_t in_ch_layout = pCodecCtx->channel_layout;

    // 输出的声道布局，这里默认使用立体声的声道布局
    uint64_t out_ch_layout = AV_CH_LAYOUT_STEREO;

    // 设置采样的配置
    swr_alloc_set_opts(swr_context,
                       out_ch_layout, out_sample_fmt, out_sample_rate,
                       in_ch_layout, in_sample_fmt, in_sample_rate,
                       0, NULL);
    // 初始化采样
    swr_init(swr_context);

    //输出的声道个数
    int out_ch_layout_number = av_get_channel_layout_nb_channels(out_ch_layout);
    // 重采样设置参数-------------------end

    // 打开输出文件
    FILE *fp_pcm = fopen(output_cstr, "wb");
    if (fp_pcm == NULL) {
        LOGE("无法打开输出文件");

    } else {
        // 定义一个缓存区存放解码后的PCM（16bit，44100）数据
        uint8_t *out_buffer = (uint8_t *) av_malloc(MAX_AUDIO_FRME_SIZE);

        clock_t start_time, finish_time;

        // 不断读取压缩数据
        int got_frame = 0, frame_count = 0;
        int result;
        start_time = clock();
        while (av_read_frame(pFormatCtx, packet) >= 0) {
            if (packet->stream_index == audio_stream_index) {
                frame_count++;
                // 对音频进行解码
                result = avcodec_decode_audio4(pCodecCtx, frame, &got_frame, packet);

                // 小于0表示解码出错
                if (result < 0) {
                    LOGI("解码第%d帧出现异常", frame_count);
                    continue;
                }

                // 大于0表示正在解码
                if (got_frame > 0) {
                    LOGI("解码到第%d帧", frame_count);
                    // 将音频数据转为指定格式的音频数据，然后存放到缓存区
                    swr_convert(swr_context, &out_buffer, MAX_AUDIO_FRME_SIZE,
                                (const uint8_t **) frame->data, frame->nb_samples);

                    // 获取音频所占用字节数的大小
                    int buffer_size = av_samples_get_buffer_size(NULL, out_ch_layout_number,
                                                                 frame->nb_samples, out_sample_fmt,
                                                                 1);

                    // 将缓存区的数据写到输出文件
                    fwrite(out_buffer, 1, (size_t) buffer_size, fp_pcm);
                }
            }

            // 每解码一帧后就释放packet资源
            av_packet_unref(packet);
        }

        finish_time = clock();
        LOGI("耗时%lf秒", (finish_time - start_time) / 1000 / 1000.0);

        // 释放缓存区
        av_free(out_buffer);

        // 关闭输出文件
        fclose(fp_pcm);
    }

    // 释放 AVFrame
    av_frame_free(&frame);

    // 释放采样上下文
    swr_close(swr_context);
    swr_free(&swr_context);

    free_codec:
    // 释放解码器上下文
    avcodec_close(pCodecCtx);
    avcodec_free_context(&pCodecCtx);

    free_format:
    // 释放封装上下文
    avformat_close_input(&pFormatCtx);
    avformat_free_context(pFormatCtx);
    (*env)->ReleaseStringUTFChars(env, input_path, input_cstr);
    (*env)->ReleaseStringUTFChars(env, output_path, output_cstr);
}