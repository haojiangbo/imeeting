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

namespace myDecode{

    AVCodecContext *c= NULL;
    AVCodecParserContext *parser = NULL;
    AVCodec *codec;
    AVPacket *pkt;
    AVFrame *decoded_frame = NULL;
    JtoolUtils jtoolUtils;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_AudioDecode_initContext(JNIEnv *env, jobject thiz) {

    // 解码器
    myDecode::codec = avcodec_find_decoder(AV_CODEC_ID_MP2);
    if (!myDecode::codec) {
        ALOGE("Codec not found\n");
        return;
    }
    // 解析器
    myDecode::parser = av_parser_init(myDecode::codec->id);
    if (!myDecode::parser) {
        ALOGE("Parser not found\n");
        return;
    }
    //创建解码器上下文
    myDecode::c = avcodec_alloc_context3(myDecode::codec);
    if (!myDecode::c) {
        ALOGE("Could not allocate audio codec context\n");
        return;
    }
    //打开编码器
    if (avcodec_open2(myDecode::c, myDecode::codec, NULL) < 0) {
        ALOGE("Could not open codec\n");
        return;
    }
    // 创建一个decode帧
    myDecode::decoded_frame = av_frame_alloc();
    // 创建一个包
    myDecode::pkt = av_packet_alloc();
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_haojiangbo_ffmpeg_AudioDecode_decodeFrame(JNIEnv *env, jobject thiz, jbyteArray bytes) {
    char * dataBuff = myDecode::jtoolUtils.jarray2charponit(env,bytes);
    // 把数据包解析到packet里
    int ret = av_parser_parse2(myDecode::parser, myDecode::c, &myDecode::pkt->data, &myDecode::pkt->size,
                               (uint8_t*)dataBuff, myDecode::jtoolUtils.charLen,
                               AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
    if (ret < 0) {
        ALOGE("Error while parsing\n");
        return NULL;
    }

    /* send the packet with the compressed data to the decoder */
    ret = avcodec_send_packet(myDecode::c, myDecode::pkt);
    if (ret < 0) {
        ALOGE("Error submitting the packet to the decoder\n");
        return NULL;
    }

    /* read all the output frames (in general there may be any number of them */
    while (ret >= 0) {
        ret = avcodec_receive_frame(myDecode::c, myDecode::decoded_frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            break;
        else if (ret < 0) {
            ALOGE("Error during decoding\n");
            return NULL;
        }
    }
    return NULL;
}



extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_AudioDecode_freeContext(JNIEnv *env, jobject thiz) {
    avcodec_free_context(&myDecode::c);
    av_parser_close(myDecode::parser);
    av_frame_free(&myDecode::decoded_frame);
    av_packet_free(&myDecode::pkt);
}