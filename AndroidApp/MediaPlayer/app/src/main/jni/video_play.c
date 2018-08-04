#include "com_ytempest_mediaplayer_MediaPlayer.h"
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include "libavutil/imgutils.h"

//编码
#include "libavcodec/avcodec.h"
//封装格式处理
#include "libavformat/avformat.h"
//像素处理
#include "libswscale/swscale.h"
#include "libyuv.h"

#include <android/native_window.h>
#include <android/native_window_jni.h>

#include <android/log.h>

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO, "video_player",CONTENT,##__VA_ARGS__)
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"video_player",CONTENT,##__VA_ARGS__)

// 日志
void custom_log(void *ptr, int level, const char *fmt, va_list vl) {
    FILE *fp = fopen("/storage/emulated/0/aaa/render/av_log.txt", "a+");
    if (fp) {
        vfprintf(fp, fmt, vl);
        fflush(fp);
        fclose(fp);
    }
}

JNIEXPORT void JNICALL Java_com_ytempest_mediaplayer_MediaPlayer_playVideo
        (JNIEnv *env, jobject jobj, jstring input_path, jobject surface) {
    //需要转码的视频文件(输入的视频文件)
    const char *input_cstr = (*env)->GetStringUTFChars(env, input_path, NULL);

    av_log_set_callback(custom_log);

    // 1.注册所有组件
    av_register_all();

    // 封装格式上下文，统领全局的结构体，保存了视频文件封装格式的相关信息
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    // 2.打开输入视频文件
    if (avformat_open_input(&pFormatCtx, input_cstr, NULL, NULL) != 0) {
        LOGE("%s", "无法打开输入视频文件");
        goto free_format;
    }

    // 3.获取视频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("%s", "无法获取视频文件信息");
        goto free_format;
    }

    //获取视频流的索引位置
    //遍历所有类型的流（音频流、视频流、字幕流），找到视频流
    int v_stream_index = -1;
    int i = 0;
    for (; i < pFormatCtx->nb_streams; i++) {
        //流的类型
        // pFormatCtx->streams[i]->codec已经过时了
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            v_stream_index = i;
            break;
        }
    }
    if (v_stream_index == -1) {
        LOGE("%s", "找不到视频流");
        goto free_format;
    }


    // 只有知道视频的编码方式，才能够根据编码方式去找到解码器
    // 获取视频流中的编解码上下文
    AVCodecContext *pCodecCtx = pFormatCtx->streams[v_stream_index]->codec;

    // 4.根据编解码上下文中的编码id查找对应的解码器
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

    //（迅雷看看，找不到解码器，临时下载一个解码器）
    if (pCodec == NULL) {
        LOGE("%s", "找不到解码器");
        goto free_codec;
    }

    // 5.打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("%s", "解码器无法打开");
        goto free_codec;
    }

    // 输出视频信息
    LOGI("视频的文件格式：%s", pFormatCtx->iformat->name);
    LOGI("视频时长：%lf秒", (pFormatCtx->duration) / 1000 / 1000.0);
    LOGI("视频的宽高：%d x %d", pCodecCtx->width, pCodecCtx->height);
    LOGI("解码器的名称：%s", pCodec->name);
    LOGI("视频数据格式 : %s", av_get_pix_fmt_name(pCodecCtx->pix_fmt));

    // AVPacket用于存储一帧一帧的压缩数据（H264）
    // 缓冲区，开辟空间，用于保存一帧数据，这一帧数据包括(视频流数据、音频流数据、字幕流数据等)
    AVPacket *packet = (AVPacket *) malloc(sizeof(AVPacket));
    // AVFrame用于存储解码后的像素数据(YUV)，分配内存
    AVFrame *pFrameYUV = av_frame_alloc();
    // YUV420转成RGB8888格式
    AVFrame *pFrameRGB = av_frame_alloc();

    // native绘制，Android的窗口也是这样绘制的
    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    // 绘制时的缓冲区
    ANativeWindow_Buffer out_buffer;

    int got_frame, result;
    int frame_count = 0;

    // 6.一帧一帧的读取压缩数据，读取到的数据都保存在了 packet中
    while (av_read_frame(pFormatCtx, packet) >= 0) {
        // 根据流的索引位置判断，只要是视频压缩数据就进行读取
        if (packet->stream_index == v_stream_index) {
            // 7.从packet中获取视频像素数据
            // 像素数据保存在pFrame、解码结果保存在got_frame、帧数据保存在packet
            result = avcodec_decode_video2(pCodecCtx, pFrameYUV, &got_frame, packet);
            if (result < 0) {
                LOGE("%s", "解码错误");
                return;
            }

            // got_frame为0说明解码完成，非0表示正在解码
            if (got_frame) {
                frame_count++;
                LOGI("解码第%d帧", frame_count);

                // 设置缓冲区的属性（宽、高、像素格式），然后锁定窗口
                ANativeWindow_setBuffersGeometry(nativeWindow, pCodecCtx->width, pCodecCtx->height,
                                                 WINDOW_FORMAT_RGBA_8888);
                ANativeWindow_lock(nativeWindow, &out_buffer, NULL);

                // 设置pFrameRGB的属性（像素格式、宽高）和缓冲区
                // pFrameRGB缓冲区与outBuffer.bits是同一块内存
                av_image_fill_arrays(pFrameRGB->data, pFrameRGB->linesize,
                                     out_buffer.bits, AV_PIX_FMT_RGBA,
                                     pCodecCtx->width, pCodecCtx->height, 1);

                // 使用libyuv库 YUV->RGBA_8888
                I420ToARGB(pFrameYUV->data[0], pFrameYUV->linesize[0],
                           pFrameYUV->data[2], pFrameYUV->linesize[2],
                           pFrameYUV->data[1], pFrameYUV->linesize[1],
                           pFrameRGB->data[0], pFrameRGB->linesize[0],
                           pCodecCtx->width, pCodecCtx->height);

                // unlock
                ANativeWindow_unlockAndPost(nativeWindow);

                // 睡眠16毫秒，这个有什么作用
                // usleep(1000 * 16);
            }
        }

        // 每读取完一帧就释放packet资源
        av_packet_unref(packet);
    }

    ANativeWindow_release(nativeWindow);

    av_frame_free(&pFrameYUV);
    av_frame_free(&pFrameRGB);

    free_codec:
    avcodec_close(pCodecCtx);
    avcodec_free_context(&pCodecCtx);

    free_format:
    avformat_close_input(&pFormatCtx);
    avformat_free_context(pFormatCtx);
    // 释放字符串资源
    (*env)->ReleaseStringUTFChars(env, input_path, input_cstr);
}