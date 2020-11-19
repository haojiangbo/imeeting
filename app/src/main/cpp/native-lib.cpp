#include <jni.h>
#include <string>
#include "stdio.h"
#include "JtoolUtils.h"
#include "log/Hlog.h"
extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>
#include <libavutil/opt.h>
#include <libavutil/channel_layout.h>
#include <libavutil/samplefmt.h>
#include "libavutil/imgutils.h"
#include <libswresample/swresample.h>
#include <libavdevice/avdevice.h>
}


extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ndkdemo_MainActivity_startMp3(JNIEnv *env, jobject thiz, jstring url) {
    char *filePath = (char *) env->GetStringUTFChars(url, NULL);
    ALOGE("filePath == %s", filePath);
    AVFormatContext *formatContext;
    // 注册所有编码器
    av_register_all();
    // 支持网络
    avformat_network_init();
    // 得到格式化工厂的上下文
    formatContext = avformat_alloc_context();
    //开始准备解复用
    int r = avformat_open_input(&formatContext,filePath,NULL,NULL);
    if(r != 0){
        ALOGE("avformat_open_input open error %d \n",r);
        return ;
    }
    // 转储格式信息
    av_dump_format(formatContext, -1, NULL, 0);
    // 查找流
    r = avformat_find_stream_info(formatContext,NULL);
    if(r < 0){
        ALOGE("avformat_find_stream_info not find stream \n");
        return ;
    }
    // 找到音频流下标
    int audioIndex = -1;
    for(int i = 0; i < formatContext->nb_streams; i++){
        if(formatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO){
            audioIndex = i;
            break;
        }
    }
    if(audioIndex == -1){
        ALOGE("audio stream index not defind \n");
        return;
    }
    AVCodecContext *audioCodecContext = formatContext->streams[audioIndex]->codec;
    AVCodec *audioCodec = avcodec_find_decoder(audioCodecContext->codec_id);
    ALOGE("采样率  %d \n",audioCodecContext->sample_rate);
    ALOGE("通道  %d \n",audioCodecContext->channels);
    // 打开音频解码器
    r = avcodec_open2(audioCodecContext,audioCodec,NULL);
    if(r != 0){
        ALOGE("avcodec_open2 is error\n");
        return;
    }
    AVFrame *catchFrame = av_frame_alloc();
    int i = 0;
    //重采样
    SwrContext *swr = swr_alloc_set_opts(NULL,  // we're allocating a new context
                                         audioCodecContext->channel_layout,  // out_ch_layout
                                         AV_SAMPLE_FMT_S16,    // out_sample_fmt
                                         audioCodecContext->sample_rate,    // out_sample_rate
                                         audioCodecContext->channel_layout, // in_ch_layout
                                         audioCodecContext->sample_fmt,   // in_sample_fmt
                                         audioCodecContext->sample_rate,   // in_sample_rate
                                         0,                    // log_offset
                                         NULL);                // log_ctx
    // 初始化
    swr_init(swr);
    const int bufSize = av_samples_get_buffer_size(NULL, 1,1152, AV_SAMPLE_FMT_S16, 1);
    uint8_t* data1 = (uint8_t*)malloc(bufSize);
    uint8_t* data2 = (uint8_t*)malloc(bufSize);
    memset(data1, 0, bufSize);
    memset(data2, 0, bufSize);
    uint8_t *audio_data_buffer[2] = { data1, data2 };
    AVPacket avPacket;
    while (av_read_frame(formatContext,&avPacket) >= 0){
        if(avPacket.stream_index == audioIndex){
            int isOk = 0;
            // 开始解码
            int r =  avcodec_decode_audio4(audioCodecContext,catchFrame,&isOk,&avPacket);
            if(r < 0){
                continue;
            }
            int isPlanar =  av_sample_fmt_is_planar(audioCodecContext->sample_fmt);
            if(isOk){
                i++;
                int  ret = swr_convert(swr, audio_data_buffer, catchFrame->nb_samples, (const uint8_t **) catchFrame->extended_data, catchFrame->nb_samples);
                ALOGE("swr_convert >>>> %d ", ret);

                //通过反射调用java中的方法
                //找class 使用FindClass方法，参数就是要调用的函数的类的完全限定名，但是需要把点换成/
                jclass clazz = env->FindClass("com/haojiangbo/ndkdemo/MainActivity");
                //获取对应的函数: 参数1:类class,参数2:方法名,参数3:方法签名
                //ps:方法签名的获取:进入build->intermediates->classes->debug目录下,使用javap -s 类的完全限定名,就能获得函数签名
                jmethodID method = env->GetMethodID(clazz, "printRet", "(I)V");
                //实例化该class对应的实例  使用AllocObject方法，使用clazz创建该class的实例。
                //调用方法
                env->CallVoidMethod(thiz, method,ret);
            }
        }
    }
    av_frame_free(&catchFrame);
    avcodec_close(audioCodecContext);
    avformat_close_input(&formatContext);
}
