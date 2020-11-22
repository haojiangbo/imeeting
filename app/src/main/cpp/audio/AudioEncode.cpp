//
// Created by Administrator on 2020/11/19.
//
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



AVCodec *codec;
AVCodecContext *c= NULL;
AVFrame *frame;
AVPacket *pkt;

extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_AudioEncode_initContext(JNIEnv *env, jobject thiz) {
    /* find the MP2 encoder */
    codec = avcodec_find_encoder(AV_CODEC_ID_MP2);
    if (!codec) {
        ALOGE("Codec not found\n");
        return;
    }
    c = avcodec_alloc_context3(codec);
    if (!c) {
        ALOGE("Could not allocate audio codec context\n");
        return;
    }
    //long byteRate = sample_fmt * mSampleRate * channels / 8;
    c->bit_rate = 64000  ;
    /*16位/样本*/
    c->sample_fmt = AV_SAMPLE_FMT_S16;
    /* select other audio parameters supported by the encoder */
    c->sample_rate    = 44100;
    // 声道布局 单声道
    c->channel_layout = AV_CH_LAYOUT_MONO;
    // 通道数量
    c->channels       = av_get_channel_layout_nb_channels(c->channel_layout);
    /*打开编码器*/
    if (avcodec_open2(c, codec, NULL) < 0) {
        ALOGE("Could not open codec\n");
        return;
    }
    ALOGE("open codec  OK \n");
    pkt = av_packet_alloc();
    if (!pkt) {
        ALOGE("could not allocate the packet\n");
        return;
    }
    /* 创建一个数据帧 */
    frame = av_frame_alloc();
    if (!frame) {
        ALOGE("Could not allocate audio frame\n");
        return;
    }
    // 设置帧的一些参数
    frame->nb_samples     = c->frame_size;
    frame->format         = c->sample_fmt;
    frame->channel_layout = c->channel_layout;
    int  ret = av_frame_get_buffer(frame, 0);
    if (ret < 0) {
        ALOGE("Could not allocate audio data buffers\n");
        return;
    }
    ALOGE("audio encode ok\n");
}


// 转换工具类
JtoolUtils jtoolUtils;
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_haojiangbo_ffmpeg_AudioEncode_encodeFrame(JNIEnv *env, jobject thiz, jbyteArray bytes) {
    char * data =  jtoolUtils.jarray2charponit(env,bytes);
    // 指针拷贝 用于后面的内存释放
    char * freeDataPoint = data;
    ALOGE("111111 inputDataLen = %d,  nb_samples = %d ",jtoolUtils.charLen,frame->nb_samples);
    int srcLen = jtoolUtils.charLen;
    int totalSamples = jtoolUtils.charLen;
    int constSamplesLen = frame->nb_samples;
    int totalResultIndex = 0;
    char *resultBuff = (char *)malloc(jtoolUtils.charLen);
    // 样本数转换 转换成实际的占用字节数
    int tmpSamplesLen = frame->nb_samples * 2;
    while (totalSamples > 0){
        //frame->data[0]
        // 比较大小，如果剩余数据 小于 样本数据 就 返回 剩余数据
        int tmp =  totalSamples > tmpSamplesLen ? tmpSamplesLen : totalSamples;
        frame->data[0] = (uint8_t*)data;
        // 移动数据指针
        data += tmp;
        totalSamples -= tmp;
        // 重新修改样本数
        // 此处为什么 codecContext也要赋值呢 因为样本不一致 导致数据出问题
        frame->nb_samples = tmp  / 2;
        c->frame_size = frame->nb_samples;
        int ret;
        /* 发送帧到帧编码器 */
        ret = avcodec_send_frame(c, frame);
        if (ret < 0) {
            ALOGE("Error sending the frame to the encoder %s\n ",ret);
            continue;
        }
        ALOGE("222222 inputDataLen = %d,  nb_samples = %d ",jtoolUtils.charLen,frame->nb_samples);
        while (ret >= 0) {
            ret = avcodec_receive_packet(c, pkt);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
                break;
            else if (ret < 0) {
                ALOGE("Error encoding audio frame\n %d ",ret);
                break;
            }
            // 拷贝编码后的值
            memcpy(resultBuff + totalResultIndex,pkt->data,pkt->size);
            totalResultIndex += pkt->size;
            av_packet_unref(pkt);
        }
    }
    //恢复样本数
    frame->nb_samples = constSamplesLen;
    c->frame_size = frame->nb_samples;
    jtoolUtils.charLen = totalResultIndex;
    jbyteArray  r = jtoolUtils.charpoint2jarray(env,resultBuff);
    free(freeDataPoint);
    free(resultBuff);
    ALOGE("原始长度 = %d , 压缩后的长度 %d",srcLen,totalResultIndex);
    return r;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_AudioEncode_freeContext(JNIEnv *env, jobject thiz) {
    av_frame_free(&frame);
    av_packet_free(&pkt);
    avcodec_free_context(&c);
}