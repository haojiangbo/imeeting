//
// Created by haojiangbo on 2022/5/29.
//

#include <jni.h>
#include <string>
#include "stdio.h"
#include "JtoolUtils.h"
#include "log/Hlog.h"
#include "../common/CodecConfig.h"

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
}

struct AudioContext {
    AVCodecContext *c = NULL;
    AVCodecParserContext *parser = NULL;
    AVCodec *codec;
    AVPacket *pkt;
    AVFrame *decoded_frame = NULL;
    JtoolUtils jtoolUtils;
};

extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_AudioDecodeObj_initContext(JNIEnv *env, jobject thiz) {
    struct AudioContext *context = (AudioContext *) malloc(sizeof(struct AudioContext));
    // 解码器
    context->codec = avcodec_find_decoder(AUDIO_CODE);
    if (!context->codec) {
        ALOGE("Codec not found\n");
        return;
    }
    // 解析器
    context->parser = av_parser_init(context->codec->id);
    if (!context->parser) {
        ALOGE("Parser not found\n");
        return;
    }
    //创建解码器上下文
    context->c = avcodec_alloc_context3(context->codec);
    if (!context->c) {
        ALOGE("Could not allocate audio codec context\n");
        return;
    }
    //打开编码器
    if (avcodec_open2(context->c, context->codec, NULL) < 0) {
        ALOGE("Could not open codec\n");
        return;
    }
    // 创建一个decode帧
    context->decoded_frame = av_frame_alloc();
    // 创建一个包
    context->pkt = av_packet_alloc();
    ALOGE("audio decode ok\n");

    jclass clazz = (jclass) env->GetObjectClass(thiz);
    jfieldID fid = (jfieldID) (*env).GetFieldID(clazz, "decodeContext", "J");
    env->SetLongField(thiz, fid, (jlong) context);
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_haojiangbo_ffmpeg_AudioDecodeObj_decodeFrame(JNIEnv *env, jobject thiz, jbyteArray bytes) {


    jclass objClazz = (jclass) env->GetObjectClass(thiz);//obj为对应的JAVA对象
    jfieldID fid = env->GetFieldID(objClazz, "decodeContext", "J");
    jlong p = (jlong) env->GetLongField(thiz, fid);
    struct AudioContext *contxt = (AudioContext *) p;

    if(contxt->c == NULL){
        return NULL;
    }


    char *dataBuff = contxt->jtoolUtils.jarray2charponit(env, bytes);
    // 把数据包解析到packet里
    int ret = av_parser_parse2(contxt->parser, contxt->c, &contxt->pkt->data,
                               &contxt->pkt->size,
                               (uint8_t *) dataBuff, contxt->jtoolUtils.charLen,
                               AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
    if (ret < 0) {
        ALOGE("Error while parsing\n");
        return NULL;
    }

    // 把原始数据包数据发送到 解码器中
    ret = avcodec_send_packet(contxt->c, contxt->pkt);
    if (ret < 0) {
        ALOGE("Error submitting the packet to the decoder\n");
        return NULL;
    }

    // 从codeContext接收一帧数据
//    int totalBuffLen = 1024 * 3;
//    char * resultBuff = (char *) malloc(totalBuffLen);
    while (ret >= 0) {
        ret = avcodec_receive_frame(contxt->c, contxt->decoded_frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            break;
        else if (ret < 0) {
            ALOGE("Error during decoding\n");
            return NULL;
        }
        contxt->jtoolUtils.charLen =  contxt->decoded_frame->linesize[0];
        return contxt->jtoolUtils.
                charpoint2jarray(env,(char *)contxt->decoded_frame->data[0]);
//        if (totalBuffLen < myDecode::decoded_frame->linesize[0]){
//            totalBuffLen += myDecode::decoded_frame->linesize[0] - totalBuffLen;
//            realloc(resultBuff,totalBuffLen);
//        }
//        memcpy(resultBuff, myDecode::decoded_frame->data[0], myDecode::decoded_frame->linesize[0]);
    }

    return NULL;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_AudioDecodeObj_freeContext(JNIEnv *env, jobject thiz) {

    jclass objClazz = (jclass) env->GetObjectClass(thiz);//obj为对应的JAVA对象
    jfieldID fid = env->GetFieldID(objClazz, "decodeContext", "J");
    jlong p = (jlong) env->GetLongField(thiz, fid);
    struct AudioContext *contxt = (AudioContext *) p;

    avcodec_free_context(&contxt->c);
    av_parser_close(contxt->parser);
    av_frame_free(&contxt->decoded_frame);
    av_packet_free(&contxt->pkt);
}