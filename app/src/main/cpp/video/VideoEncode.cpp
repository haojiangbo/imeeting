//
// Created by Administrator on 2020/11/21.
//
#include <jni.h>
#include <string>
#include "stdio.h"
#include "JtoolUtils.h"
#include "log/Hlog.h"

jbyteArray encode(const JNIEnv *env);

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



namespace VideoEncode {
    char * codeName = "mpeg1video";
    const AVCodec *codec;
    AVCodecContext *c= NULL;
    AVFrame *frame;
    AVPacket *pkt;
    JtoolUtils jtoolUtils;
    int pts = 0;
}


jbyteArray encode(JNIEnv *env) {
    int ret = avcodec_send_frame(VideoEncode::c, VideoEncode::frame);
    if (ret < 0) {
        ALOGE( "Error sending a frame for encoding\n");
        return NULL;
    }

    while (ret >= 0) {
        ret = avcodec_receive_packet(VideoEncode::c, VideoEncode::pkt);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
            return NULL;
        else if (ret < 0) {
            ALOGE("Error during encoding\n");
            return NULL;
        }
        /*printf("Write packet %3"" (size=%5d)\n", pkt->pts, pkt->size);
        fwrite(pkt->data, 1, pkt->size, outfile);*/
        char * resultBuff = (char *)malloc(VideoEncode::pkt->size);
        VideoEncode::jtoolUtils.charLen = VideoEncode::pkt->size;
        memcpy(resultBuff,VideoEncode::pkt->data,VideoEncode::pkt->size);
        av_packet_unref(VideoEncode::pkt);
        return VideoEncode::jtoolUtils.charpoint2jarray(env,resultBuff);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoEncode_initContext(JNIEnv *env, jobject thiz) {
    ALOGE("init videEncode ...");
    /* find the mpeg1video encoder */
    // VideoEncode::codec = avcodec_find_encoder_by_name(VideoEncode::codeName);
    //VideoEncode::codec = avcodec_find_encoder(AV_CODEC_ID_H264);
    VideoEncode::codec = avcodec_find_encoder(AV_CODEC_ID_MPEG4);
    if (!VideoEncode::codec) {
        ALOGE("Codec '%s' not found\n", VideoEncode::codeName);
        return;
    }
    VideoEncode::c = avcodec_alloc_context3(VideoEncode::codec);
    if (! VideoEncode::c) {
        ALOGE( "Could not allocate video codec context\n");
        return;
    }
    VideoEncode::pkt = av_packet_alloc();
    if (!VideoEncode::pkt){
        ALOGE( "Could not alloc pkt\n");
        return;
    }

    /* put sample parameters */
    VideoEncode::c->bit_rate = 512 * 1000;
    /* resolution must be a multiple of two */
    VideoEncode::c->width = 640;
    VideoEncode::c->height = 480;
    /* frames per second */
    VideoEncode::c->time_base = (AVRational){1, 25};
    VideoEncode::c->framerate = (AVRational){25, 1};


    /* emit one intra frame every ten frames
     * check frame pict_type before passing frame
     * to encoder, if frame->pict_type is AV_PICTURE_TYPE_I
     * then gop_size is ignored and the output of encoder
     * will always be I frame irrespective to gop_size
     */
    VideoEncode::c->gop_size = 10;
    VideoEncode::c->max_b_frames = 1;
    VideoEncode::c->pix_fmt = AV_PIX_FMT_YUV420P;

    // 若是h264编码器，要设置一些参数
    AVDictionary *param = 0;
    if (VideoEncode::c->codec_id == AV_CODEC_ID_H264) {
        // https://www.jianshu.com/p/b46a33dd958d
        av_dict_set(&param, "preset", "slow", 0);
        av_dict_set(&param, "tune", "zerolatency", 0);
    }
    /* open it */
    int  ret = avcodec_open2(VideoEncode::c, VideoEncode::codec, &param);
    if (ret < 0) {
        ALOGE( "Could not open codec: %s\n", av_err2str(ret));
        return;
    }

    VideoEncode::frame = av_frame_alloc();
    if (!VideoEncode::frame) {
        ALOGE( "Could not allocate video frame\n");
        return;
    }
    VideoEncode::frame->format = VideoEncode::c->pix_fmt;
    VideoEncode::frame->width  = VideoEncode::c->width;
    VideoEncode::frame->height = VideoEncode::c->height;

    ret = av_frame_get_buffer(VideoEncode::frame, 0);
    if (ret < 0) {
        ALOGE( "Could not allocate the video frame data\n");
        return;
    }
    //int buffSize = avpicture_get_size(AV_PIX_FMT_YUV420P, VideoEncode::c->width, VideoEncode::c->height);
    ALOGE("init videEncode OK");
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_haojiangbo_ffmpeg_VideoEncode_encodeFrame(JNIEnv *env, jobject thiz, jbyteArray bytes) {

    char * data =  VideoEncode::jtoolUtils.jarray2charponit(env,bytes);
    memcpy(VideoEncode::frame->data[0],data, VideoEncode::jtoolUtils.charLen);
    // https://blog.csdn.net/chinabinlang/article/details/7804808
    // 向这个博客致敬  salute
    int size = VideoEncode::c->width * VideoEncode::c->height;
    VideoEncode::frame->data[1] = VideoEncode::frame->data[0] + size;
    VideoEncode::frame->data[2] = VideoEncode::frame->data[1] + size / 4;
    free(data);
    return encode(env);
}



extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoEncode_freeContext(JNIEnv *env, jobject thiz) {
    // TODO: implement freeContext()
    avcodec_free_context(&VideoEncode::c);
    av_frame_free(&VideoEncode::frame);
    av_packet_free(&VideoEncode::pkt);
}


