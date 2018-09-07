#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <pthread.h>
#include "libavutil/imgutils.h"

//编码
#include "libavcodec/avcodec.h"
//封装格式处理
#include "libavformat/avformat.h"
//像素处理
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libyuv.h"

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>

#include "com_ytempest_mediaplayer_MediaPlayer.h"
#include "Queue.h"

#define LOGI(CONTENT, ...) __android_log_print(ANDROID_LOG_INFO, "video_player",CONTENT,##__VA_ARGS__)
#define LOGE(CONTENT, ...) __android_log_print(ANDROID_LOG_ERROR,"video_player",CONTENT,##__VA_ARGS__)

// nb_streams，视频文件中存在视频流、音频流、字幕流（暂不考虑）
#define MAX_STREAM 2

#define MAX_AUDIO_FRME_SIZE 4100*2

// AVPacket队列的长度
#define PACKET_QUEUE_SIZE 50

/**
 * 封装了音视频解码需要的相关参数
 */
typedef struct Player {
    JavaVM *javaVM;

    // 音频视频流的索引位置
    int video_stream_index;
    int audio_stream_index;

    // 音频视频的解码器上下文
    AVCodecContext *input_codec_ctx[MAX_STREAM];

    // 音频视频解码线程id
    pthread_t decode_threads[MAX_STREAM];

    // 视频解码所需要的参数信息
    // 封装格式的上下文
    AVFormatContext *input_format_ctx;
    ANativeWindow *nativeWindow;


    // 音频解码所需要的参数信息
    SwrContext *swr_ctx;
    // 输入音频、输出音频的音频格式
    enum AVSampleFormat in_sample_fmt;
    enum AVSampleFormat out_sample_fmt;

    // 输入音频、输出音频的采样率
    int in_sample_rate;
    int out_sample_rate;

    // 输出音频的声道个数
    int out_channel_number;

    // Jni AudioTrack
    jobject audio_track;
    jmethodID audio_track_write_mid;

    // 生产者线程
    pthread_t thread_read_from_stream;
    // 音频、视频的AVPacket队列
    Queue *packets[MAX_STREAM];
    // 流的总个数
    int capture_stream_no;
} Player;


/**
 * 解码数据结构体
 */
typedef struct _DecoderData {
    Player *player;
    int stream_index;
} DecoderData;


/**
 * 初始化封装格式的上下文，获取音视频流的索引位置
 * @param player 封装了音视频解码需要的相关参数
 * @param input_cstr  视频文件的路径
 */
void init_input_format_context(Player *player, const char *input_cstr) {
    // 1.注册所有组件
    av_register_all();

    // 2.封装格式上下文，统领全局的结构体，保存了视频文件封装格式的相关信息
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    // 3.打开输入视频文件
    if (avformat_open_input(&pFormatCtx, input_cstr, NULL, NULL) != 0) {
        LOGE("%s", "无法打开输入视频文件");
        return;
    }

    // 4.获取视频文件信息
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("%s", "无法获取视频文件信息");
        return;
    }

    // 5、获取音频流、视频流的索引位置
    // 遍历所有类型的流（音频流、视频流、字幕流），找到视频流
    int i = 0;
    for (; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            player->video_stream_index = i;
        } else if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            player->audio_stream_index = i;
        }
    }

    // 6、始化Player结构体中的封装格式上下文
    player->input_format_ctx = pFormatCtx;

    // 7、保存流的数量
    player->capture_stream_no = pFormatCtx->nb_streams;
    LOGI("视频文件存在的流个数：", pFormatCtx->nb_streams);
}


/**
 * 根据流的索引初始化音频、视频或者字幕的解码器的上下文
 * @param player 封装了音视频解码需要的相关参数
 * @param stream_index  音频流、视频流、字幕流等索引
 */
void init_codec_context(Player *player, int stream_index) {
    // 只有知道视频的编码方式，才能够根据编码方式去找到解码器
    // 1、获取视频流中的编解码上下文
    AVFormatContext *pFormatCtx = player->input_format_ctx;
    AVCodecContext *pCodecCtx = pFormatCtx->streams[stream_index]->codec;

    // 2、根据编解码上下文中的编码id查找对应的解码器
    AVCodec *pCodec = avcodec_find_decoder(pCodecCtx->codec_id);

    //（迅雷看看，找不到解码器，临时下载一个解码器）
    if (pCodec == NULL) {
        LOGE("%s", "找不到解码器");
        return;
    }

    // 打开解码器
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("%s", "解码器无法打开");
        return;
    }

    // 3、初始化Player中的解码器上下文
    player->input_codec_ctx[stream_index] = pCodecCtx;

    // 输出视频信息
    LOGI("视频的文件格式：%s", pFormatCtx->iformat->name);
    LOGI("视频时长：%lf秒", (pFormatCtx->duration) / 1000 / 1000.0);
    LOGI("视频的宽高：%d x %d", pCodecCtx->width, pCodecCtx->height);
    LOGI("解码器的名称：%s", pCodec->name);
    LOGI("视频数据格式 : %s", av_get_pix_fmt_name(pCodecCtx->pix_fmt));
}

/**
 * 初始化ANativeWindow，为将视频绘制到手机屏幕上做准备
 * @param env jni 环境
 * @param player 封装了音视频解码需要的相关参数
 * @param surface
 */
void decode_video_prepare(JNIEnv *env, Player *player, jobject surface) {
    // native绘制，Android的窗口也是这样绘制的
    player->nativeWindow = ANativeWindow_fromSurface(env, surface);
}


/**
 * 为播放音频做一些准备工作
 */
JNIEXPORT void decode_audio_prepare(Player *player) {
    AVCodecContext *pCodecCtx = player->input_codec_ctx[player->audio_stream_index];

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

    // 保存到Player结构体中
    player->in_sample_fmt = in_sample_fmt;
    player->out_sample_fmt = out_sample_fmt;
    player->in_sample_rate = in_sample_rate;
    player->out_sample_rate = out_sample_rate;
    player->swr_ctx = swr_context;
    player->out_channel_number = out_ch_number;
}


/**
 * 初始化Java的AudioTrack对象，为播放音频做准备
 * @param env 主线程的env
 * @param jobj 调用native方法的对象
 */
void jni_audio_prepare(JNIEnv *env, jobject jobj, Player *player) {
    int out_sample_rate = player->out_sample_rate;
    int out_ch_number = player->out_channel_number;

    // JNI-----------start
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
    // JNI-----------end

    // 将AudioTrack对象和write方法ID转成全局引用，后面会在子线程中用到
    player->audio_track = (*env)->NewGlobalRef(env, audio_track_obj);
    player->audio_track_write_mid = audio_track_write_mid;
}

/**
 * 从packet中解析视频的每一帧数据
 */
void decode_video_frame(Player *player, AVPacket *packet, AVFrame *pFrameYUV, AVFrame *pFrameRGB,
                        ANativeWindow_Buffer *out_buffer) {

    AVCodecContext *pCodecCtx = player->input_codec_ctx[player->video_stream_index];
    int got_frame;
    // 1、从packet中获取视频像素数据
    // 像素数据保存在pFrame、解码结果保存在got_frame、帧数据保存在packet
    int result = avcodec_decode_video2(pCodecCtx, pFrameYUV, &got_frame, packet);
    if (result < 0) {
        LOGE("%s", "解码错误");
        return;
    }

    // 2、got_frame为0说明解码完成，非0表示正在解码
    if (got_frame) {
        // 3、设置缓冲区的属性（宽、高、像素格式），然后锁定窗口
        ANativeWindow_setBuffersGeometry(player->nativeWindow, pCodecCtx->width, pCodecCtx->height,
                                         WINDOW_FORMAT_RGBA_8888);
        ANativeWindow_lock(player->nativeWindow, out_buffer, NULL);

        // 4、设置pFrameRGB的属性（像素格式、宽高）和缓冲区
        // pFrameRGB缓冲区与outBuffer.bits是同一块内存
        av_image_fill_arrays(pFrameRGB->data, pFrameRGB->linesize,
                             (*out_buffer).bits, AV_PIX_FMT_RGBA,
                             pCodecCtx->width, pCodecCtx->height, 1);

        // 5、使用libyuv库 YUV->RGBA_8888
        I420ToARGB(pFrameYUV->data[0], pFrameYUV->linesize[0],
                   pFrameYUV->data[2], pFrameYUV->linesize[2],
                   pFrameYUV->data[1], pFrameYUV->linesize[1],
                   pFrameRGB->data[0], pFrameRGB->linesize[0],
                   pCodecCtx->width, pCodecCtx->height);

        // 6、unlock
        ANativeWindow_unlockAndPost(player->nativeWindow);

        // 睡眠16毫秒
        usleep(1000 * 16);
    }
}

void
decode_audio_frame(JNIEnv *env, Player *player, AVPacket *packet, AVFrame *pFrame,
                   uint8_t *out_buffer) {
    // 1、获取一些参数
    AVCodecContext *pCodecCtx = player->input_codec_ctx[player->audio_stream_index];
    SwrContext *swr_context = player->swr_ctx;
    int out_ch_number = player->out_channel_number;
    enum AVSampleFormat out_sample_fmt = player->out_sample_fmt;
    int got_frame;

    // 2、将 packet中的音频数据解码到 AVFrame中
    int result = avcodec_decode_audio4(pCodecCtx, pFrame, &got_frame, packet);

    // 解码异常
    if (result < 0) {
        LOGE("解码出现异常");
        return;
    }

    // 解码正常
    if (got_frame > 0) {
        // 3、将音频数据转为我们重采样格式的音频数据
        swr_convert(swr_context, &out_buffer, MAX_AUDIO_FRME_SIZE,
                    (const uint8_t **) pFrame->data, pFrame->nb_samples);

        // 4、获取采样的数据大小
        int out_buffer_size = av_samples_get_buffer_size(NULL, out_ch_number,
                                                         pFrame->nb_samples, out_sample_fmt, 1);

        // 5、将 out_buffer缓存求的数据 转成 byte数组----------start
        // 创建和out_buffer相同大小的byte数组
        jbyteArray audio_sample_array = (*env)->NewByteArray(env, out_buffer_size);
        jbyte *sample_byte_p = (*env)->GetByteArrayElements(env, audio_sample_array, NULL);
        //out_buffer的数据复制到jbyte_array
        memcpy(sample_byte_p, out_buffer, (size_t) out_buffer_size);

        // 将jni的数组数据同步到java层中
        (*env)->ReleaseByteArrayElements(env, audio_sample_array, sample_byte_p, 0);
        // 将 out_buffer缓存求的数据 转成 byte数组----------end

        // 6、调用 AudioTrack.write将数据写到 AudioTrack，然后就会播放了
        (*env)->CallIntMethod(env, player->audio_track, player->audio_track_write_mid,
                              audio_sample_array, 0, out_buffer_size);

        // 7、释放byte数组局部引用，防止内存溢出
        (*env)->DeleteLocalRef(env, audio_sample_array);

        usleep(1000 * 16);
    }
}


void *decode_data(void *arg) {
    DecoderData *decoder = (DecoderData *) arg;
    Player *player = decoder->player;
    int stream_index = decoder->stream_index;
    Queue *queue = player->packets[stream_index];
    // 根据stream_index获取对应的AVPacket队列
    AVFormatContext *pFormatCtx = player->input_format_ctx;

    /*
    // 1、AVPacket用于存储一帧一帧的压缩数据（H264）
    // 缓冲区，开辟空间，用于保存一帧数据，这一帧数据包括(视频流数据、音频流数据、字幕流数据等)
    AVPacket *packet = (AVPacket *) malloc(sizeof(AVPacket));
     */

    // 2、初始化解码视频需要的一些资源
    // AVFrame用于存储解码后的像素数据(YUV)，分配内存
    AVFrame *pFrameYUV = av_frame_alloc();
    // YUV420转成RGB8888格式
    AVFrame *pFrameRGB = av_frame_alloc();
    // 绘制视频时的缓冲区
    ANativeWindow_Buffer video_out_buffer;

    // 3、初始化解码音频需要的一些资源
    // 创建一块缓冲区，用于存放解码后的音频数据
    AVFrame *audioFrame = av_frame_alloc();
    uint8_t *audio_out_buffer = (uint8_t *) av_malloc(MAX_AUDIO_FRME_SIZE);

    // 4、一帧一帧的读取压缩数据，读取到的数据都保存在了 packet中
    int video_frame_count = 0;
    int audio_frame_count = 0;
    // 关联当前线程获取JNIEnv，音频播放时会用到
    JavaVM *javaVM = player->javaVM;
    JNIEnv *env = NULL;
    (*javaVM)->AttachCurrentThread(javaVM, &env, NULL);

    AVPacket *packet;
    for (;;) {
        // 从相应的队列中获取AVPacket进行消费
        packet = (AVPacket *) queue_pop(queue);
        if (packet == NULL) {
            break;
        }
        // 4.1、解码视频
        if (stream_index == player->video_stream_index) {
            LOGI("视频解码第%d帧", ++video_frame_count);
            //decode_video_frame(player, packet, pFrameYUV, pFrameRGB, &video_out_buffer);

            // 4.2、解码音频
        } else if (stream_index == player->audio_stream_index) {
            LOGI("音频解码第%d帧", ++audio_frame_count);
            decode_audio_frame(env, player, packet, audioFrame, audio_out_buffer);
        }
    }
    /*
    while (av_read_frame(pFormatCtx, packet) >= 0) {
        // 4.1、解码视频
        if (packet->stream_index == player->video_stream_index) {
            LOGI("视频解码第%d帧", ++video_frame_count);
            //decode_video_frame(player, packet, pFrameYUV, pFrameRGB, &video_out_buffer);

            // 4.2、解码音频
        } else if (packet->stream_index == player->audio_stream_index) {
            LOGI("音频解码第%d帧", ++audio_frame_count);
            decode_audio_frame(env, player, packet, audioFrame, audio_out_buffer);
        }
        // 每读取完一帧就释放packet资源
        av_packet_unref(packet);
    }
     */

    // 解除关联
    (*javaVM)->DetachCurrentThread(javaVM);

    // 5、释放资源
    av_frame_free(&pFrameYUV);
    av_frame_free(&pFrameRGB);
    av_frame_free(&audioFrame);
    av_free(audio_out_buffer);
}

/*
void *queue_free_packet(void *data) {
    AVPacket *packet = (AVPacket *) data;
    av_packet_unref(packet);
    return 0;
}
 */

/**
 * 初始化音频，视频AVPacket队列，长度50
 * @param player
 */
void play_alloc_queues(Player *player) {
    int i = 0;
    // TODO：可能产生错误
    // 由于packets的长度为MAX_STREAM，为2；而capture_stream_no很有可能为3，因为可能会多一个字幕流
    for (; i < player->capture_stream_no; ++i) {
        Queue *queue = queue_init(PACKET_QUEUE_SIZE);
        player->packets[i] = queue;
    }
}

/**
 * 生产者线程：负责不断读取视频文件中的AVPacket，然后将其分别放入两个队列中
 */
void *player_read_from_stream(void *arg) {
    Player *player = (Player *) arg;
    int result;
    // 在栈内存上保存一个AVPacket
    AVPacket packet;
    AVPacket *pkt = &packet;
    for (;;) {
        result = av_read_frame(player->input_format_ctx, pkt);

        // 如果到文件结尾那么就break
        if (result < 0) {
            break;
        }

        // 将AVPacket保存到相应的队列中
        Queue *queue = player->packets[pkt->stream_index];
        AVPacket *packet_data = queue_push(queue);
        packet_data = pkt;

        // 示范队列怎么释放
        // queue_free(queue, queue_free_packet);
    }
}


void JNICALL Java_com_ytempest_mediaplayer_MediaPlayer_playVideo
        (JNIEnv *env, jobject jobj, jstring input_path, jobject surface) {
    // 1、创建一个Player结构体，用于存储解码过程中需要的一些参数
    Player *player = (Player *) malloc(sizeof(Player));

    // 2、需要转码的视频文件(输入的视频文件)
    const char *input_cstr = (*env)->GetStringUTFChars(env, input_path, NULL);

    // 3、初始化Player结构体中的JavaVM
    (*env)->GetJavaVM(env, &(player->javaVM));

    // 4、初始化封装格式的上下文，获取音频流和视频流的位置
    init_input_format_context(player, input_cstr);

    // 5、初始化音频和视频的解码器上下文
    int video_stream_index = player->video_stream_index;
    init_codec_context(player, video_stream_index);
    int audio_stream_index = player->audio_stream_index;
    init_codec_context(player, audio_stream_index);

    // 6、为播放视频做准备
    decode_video_prepare(env, player, surface);
    // 7、为播放音频做准备
    decode_audio_prepare(player);

    // 8、初始化播放音频需要的AudioTrack
    jni_audio_prepare(env, jobj, player);

    // 9、生产者线程，用于对视频进行解码，并将解码后的AVPacket放到相应的队列中
    pthread_create(&player->thread_read_from_stream, NULL, player_read_from_stream,
                   (void *) player);

    // 10、消费者线程，用于消费生产者线程生产的AVPacket
    // 开启一个解析视频AVPacket的线程对视频AVPacket进行消费
    DecoderData decoder_data_video = {player, video_stream_index};
    DecoderData *decoder_video = &decoder_data_video;
    pthread_create(&(player->decode_threads[video_stream_index]), NULL, decode_data,
                   (void *) decoder_video);

    // 开启一个解析音频AVPacket的线程对音频AVPacket进行消费
    DecoderData decoder_data_audio = {player, audio_stream_index};
    DecoderData *decoder_audio = &decoder_data_audio;
    pthread_create(&(player->decode_threads[audio_stream_index]), NULL, decode_data,
                   (void *) decoder_audio);

    // 10、释放资源
    (*env)->ReleaseStringUTFChars(env, input_path, input_cstr);
}
