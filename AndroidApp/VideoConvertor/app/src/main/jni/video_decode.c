#include "com_ytempest_videoconvertor_util_VideoUtils.h"
#include <android/log.h>

//编码
#include "libavcodec/avcodec.h"
//封装格式处理
#include "libavformat/avformat.h"
//像素处理
#include "libswscale/swscale.h"


#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO, "video_decode",CONTENT,##__VA_ARGS__)
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"video_decode",CONTENT,##__VA_ARGS__)


JNIEXPORT void JNICALL Java_com_ytempest_videoconvertor_util_VideoUtils_decode
        (JNIEnv *env, jclass jcls, jstring input_path, jstring output_path) {
    //需要转码的视频文件(输入的视频文件)
    const char *input_cstr = (*env)->GetStringUTFChars(env, input_path, NULL);
    const char *output_cstr = (*env)->GetStringUTFChars(env, output_path, NULL);

    // 1.注册所有组件
    av_register_all();

    // 封装格式上下文，统领全局的结构体，保存了视频文件封装格式的相关信息
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    // 2.打开输入视频文件
    if (avformat_open_input(&pFormatCtx, input_cstr, NULL, NULL) != 0) {
        LOGE("%s", "无法打开输入视频文件");
        return;
    }

    // 3.获取视频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("%s", "无法获取视频文件信息");
        return;
    }

    //获取视频流的索引位置
    //遍历所有类型的流（音频流、视频流、字幕流），找到视频流
    int v_stream_idx = -1;
    int i = 0;
    for (; i < pFormatCtx->nb_streams; i++) {
        //流的类型
        // pFormatCtx->streams[i]->codec已经过时了
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            v_stream_idx = i;
            break;
        }
    }
    if (v_stream_idx == -1) {
        LOGE("%s", "找不到视频流");
        return;
    }


    //只有知道视频的编码方式，才能够根据编码方式去找到解码器
    //获取视频流中的编解码上下文
    AVCodecContext *pCodecCtx = pFormatCtx->streams[v_stream_idx]->codec;

    //4.根据编解码上下文中的编码id查找对应的解码
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

    //（迅雷看看，找不到解码器，临时下载一个解码器）
    if (pCodec == NULL) {
        LOGE("%s", "找不到解码器");
        return;
    }

    //5.打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("%s", "解码器无法打开");
        return;
    }

    // 输出视频信息
    LOGI("视频的文件格式：%s", pFormatCtx->iformat->name);
    LOGI("视频时长：%ld秒", (pFormatCtx->duration) / 1000);
    LOGI("视频的宽高：%d x %d", pCodecCtx->width, pCodecCtx->height);
    LOGI("解码器的名称：%s", pCodec->name);

    //准备读取
    //AVPacket用于存储一帧一帧的压缩数据（H264）
    // 缓冲区，开辟空间，用于保存一帧数据
    AVPacket *packet = (AVPacket *) malloc(sizeof(AVPacket));
    // AVFrame用于存储解码后的像素数据(YUV)，分配内存
    AVFrame *pFrame = av_frame_alloc();
    // YUV420
    AVFrame *pFrameYUV = av_frame_alloc();

    //只有指定了AVFrame的像素格式、画面大小才能真正分配内存
    //缓冲区分配内存
    uint8_t *out_buffer = (uint8_t *) av_malloc(
            (size_t) avpicture_get_size(AV_PIX_FMT_YUV420P, pCodecCtx->width, pCodecCtx->height));

    // 为pFrameYUV初始化缓冲区，这样在将输入文件的像素格式转为YUV420格式时，
    // pFrameYUV才能够从pFrame中获取到数据
    avpicture_fill((AVPicture *) pFrameYUV, out_buffer, AV_PIX_FMT_YUV420P, pCodecCtx->width,
                   pCodecCtx->height);

    // 这个方法用于转码（缩放）视频的数据
    // 参数1、2：输入文件的宽高；参数3：输入文件的像素格式
    // 参数4、5：输出文件的宽高；参数6：输出文件的像素格式
    struct SwsContext *sws_ctx = sws_getContext(pCodecCtx->width, pCodecCtx->height,
                                                pCodecCtx->pix_fmt,
                                                pCodecCtx->width, pCodecCtx->height,
                                                AV_PIX_FMT_YUV420P,
                                                SWS_BICUBIC, NULL, NULL, NULL);

    int got_frame, len;

    FILE *fp_yuv = fopen(output_cstr, "wb+");
    if (fp_yuv == NULL) {
        LOGE("%s", "无法打开输出文件");
        return;
    }

    int frame_count = 0;
    clock_t start_time, finish_time;

    start_time = clock();
    // 6.一帧一帧的读取压缩数据，读取到的数据都保存在了 packet中
    while (av_read_frame(pFormatCtx, packet) >= 0) {
        // 只要视频压缩数据（根据流的索引位置判断）
        if (packet->stream_index == v_stream_idx) {
            // 7.从packet中获取视频像素数据
            // 像素数据保存在pFrame、解码结果保存在got_frame、帧数据保存在packet
            len = avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, packet);
            if (len < 0) {
                LOGE("%s", "解码错误");
                return;
            }

            // got_frame为0说明解码完成，非0表示正在解码
            if (got_frame) {
                // AVFrame -> yuvFrame：将输入文件的像素格式转为像素格式YUV420
                // 第2、6个参数：输入、输出数据
                // 第3、7个参数：输入、输出画面一行的数据的大小 AVFrame 转换是一行一行转换的
                // 第4参数：输入数据第一列要转码的位置 从0开始
                // 第5参数：输入画面的高度
                sws_scale(sws_ctx, (const uint8_t *const *) pFrame->data, pFrame->linesize, 0,
                          pCodecCtx->height,
                          pFrameYUV->data, pFrameYUV->linesize);

                //输出到YUV文件
                //AVFrame像素帧写入文件
                //data解码后的图像像素数据（音频采样数据）
                //Y 亮度 UV 色度（压缩了） 人对亮度更加敏感
                //U V 个数是Y的1/4
                int y_size = pCodecCtx->width * pCodecCtx->height;
                // pFrameYUV->data[0]：这个是YUV中的Y数据
                // pFrameYUV->data[1]：这个是YUV中的U数据
                // pFrameYUV->data[2]：这个是YUV中的V数据
                fwrite(pFrameYUV->data[0], sizeof(char), (size_t) y_size, fp_yuv);
                fwrite(pFrameYUV->data[1], sizeof(char), (size_t) (y_size / 4), fp_yuv);
                fwrite(pFrameYUV->data[2], sizeof(char), (size_t) (y_size / 4), fp_yuv);

                frame_count++;
                LOGI("解码第%d帧", frame_count);
            }
        }

        // 每读取完一帧就释放packet资源
        av_packet_unref(packet);
    }

    finish_time = clock();
    LOGI("解码耗时：%ld微秒", (finish_time - start_time));

    // 关闭输出文件
    fclose(fp_yuv);

    // 释放字符串资源
    (*env)->ReleaseStringUTFChars(env, input_path, input_cstr);
    (*env)->ReleaseStringUTFChars(env, output_path, output_cstr);

    av_free(out_buffer);
    av_frame_free(&pFrame);
    av_frame_free(&pFrameYUV);

    avcodec_close(pCodecCtx);

    avformat_close_input(&pFormatCtx);
    avformat_free_context(pFormatCtx);
}