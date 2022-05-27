//
// Created by Administrator on 2020/11/22.
//
#include <jni.h>
#include <string>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "stdio.h"
#include "JtoolUtils.h"
#include "log/Hlog.h"
#include "utils/ByteBuf.h"
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


namespace VideoDecode {
    const AVCodec *codec;
    AVCodecParserContext *parser;
    AVCodecContext *c = NULL;
    FILE *f;
    AVFrame *frame;
    AVFrame *rgbaFrame;
    int rgbaBuffSize;
    uint8_t *buff;
    AVPacket *pkt;
    JtoolUtils jtoolUtils;
    struct SwsContext *imgConvertCtx = NULL;
    int defalutWidth = 640;
    int defaultHeight = 480;
    AVPixelFormat converFormat = AV_PIX_FMT_RGBA;
    ByteBuf *byteBuf;
    int totalCatch = 0;

    char * rotate90Clockwise(int width, int height) {
        char *resultBuff = (char *) malloc(VideoDecode::rgbaBuffSize);
        int x = 0;
        int y = 0;
        int posR = 0;
        int posS = 0;
        for (x = 0; x < width; x++) {
            for (y = height - 1; y >= 0; y--) {
                posS = (y * width + x) * 4;
                resultBuff[posR + 0] = VideoDecode::rgbaFrame->data[0][posS + 0];// R
                resultBuff[posR + 1] = VideoDecode::rgbaFrame->data[0][posS + 1];// G
                resultBuff[posR + 2] = VideoDecode::rgbaFrame->data[0][posS + 2];// B
                resultBuff[posR + 3] = VideoDecode::rgbaFrame->data[0][posS + 3];// A
                posR += 4;
            }
        }
        return resultBuff;
    }
    /**
     * 逆时针
     * @param width
     * @param height
     * @return
     */
    char * rotate90Anticlockwise(int width, int height) {
        char *resultBuff = (char *) malloc(VideoDecode::rgbaBuffSize);
        int x = 0;
        int y = 0;
        int posR = 0;
        int posS = 0;
        for (x = width-1; x >= 0; x--) {
            for (y = height - 1; y >= 0; y--) {
                posS = (y * width + x) * 4;
                resultBuff[posR + 0] = VideoDecode::rgbaFrame->data[0][posS + 0];// R
                resultBuff[posR + 1] = VideoDecode::rgbaFrame->data[0][posS + 1];// G
                resultBuff[posR + 2] = VideoDecode::rgbaFrame->data[0][posS + 2];// B
                resultBuff[posR + 3] = VideoDecode::rgbaFrame->data[0][posS + 3];// A
                posR += 4;
            }
        }
        return resultBuff;
    }

    int decode(AVCodecContext *dec_ctx, AVFrame *frame, AVPacket *pkt) {
        int ret;
        // 把原始pkt 读入到解码器中
        ret = avcodec_send_packet(dec_ctx, pkt);
        if (ret < 0) {
            fprintf(stderr, "Error sending a packet for decoding\n");
            //fflush(stderr);
            return -1;
        }

        while (ret >= 0) {
            // 从解码器中读取一帧数据
            ret = avcodec_receive_frame(dec_ctx, frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF)
                return -1;
            else if (ret < 0) {
                fprintf(stderr, "Error during decoding\n");
                return -1;
            }
            if (VideoDecode::imgConvertCtx == NULL) {
                VideoDecode::imgConvertCtx = sws_getContext(dec_ctx->width, dec_ctx->height,
                                                            dec_ctx->pix_fmt, dec_ctx->width,
                                                            dec_ctx->height,
                                                            VideoDecode::converFormat, SWS_BICUBIC,
                                                            NULL, NULL, NULL);
            }
            // 转换数据 转换为目标 格式
            sws_scale(VideoDecode::imgConvertCtx, frame->data, frame->linesize,
                      0, dec_ctx->height,
                      VideoDecode::rgbaFrame->data, VideoDecode::rgbaFrame->linesize);
            ALOGE("saving frame %3d\n", dec_ctx->frame_number);
            fflush(stdout);
            return 1;
        }
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecode_initContext(JNIEnv *env, jobject thiz) {
    VideoDecode::pkt = av_packet_alloc();
    if (!VideoDecode::pkt) {
        ALOGE("pkt malloc error");
        return;
    }

    /* find the MPEG-1 video decoder */
    VideoDecode::codec = avcodec_find_decoder(VIDEO_CODE);
    if (!VideoDecode::codec) {
        ALOGE("Codec not found\n");
        return;
    }

    VideoDecode::parser = av_parser_init(VideoDecode::codec->id);
    if (!VideoDecode::parser) {
        ALOGE("parser not found\n");
        return;
    }

    VideoDecode::c = avcodec_alloc_context3(VideoDecode::codec);
    if (!VideoDecode::c) {
        ALOGE("Could not allocate video codec context\n");
        return;
    }

    /* open it */
    if (avcodec_open2(VideoDecode::c, VideoDecode::codec, NULL) < 0) {
        ALOGE("Could not open codec\n");
        return;
    }

    VideoDecode::frame = av_frame_alloc();
    if (!VideoDecode::frame) {
        ALOGE("Could not allocate video frame\n");
        return;
    }
    VideoDecode::rgbaFrame = av_frame_alloc();
    if (!VideoDecode::rgbaFrame) {
        ALOGE("Could not allocate video frame\n");
        return;
    }

    VideoDecode::rgbaBuffSize = avpicture_get_size(VideoDecode::converFormat,
                                                   VideoDecode::defalutWidth,
                                                   VideoDecode::defaultHeight);
    VideoDecode::buff = (uint8_t *) av_malloc(VideoDecode::rgbaBuffSize * (sizeof(uint8_t)));
    // 填充帧内的数据 比如 linesize[]  和 data[]
    avpicture_fill((AVPicture *) VideoDecode::rgbaFrame, VideoDecode::buff,
                   VideoDecode::converFormat, VideoDecode::defalutWidth,
                   VideoDecode::defaultHeight);

    VideoDecode::byteBuf = newByteBuf();

    ALOGE("video decode init ok");
}


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecode_decodeFrame(JNIEnv *env, jobject thiz, jbyteArray bytes) {
    char *dataBuff = VideoDecode::jtoolUtils.jarray2charponit(env, bytes);
    int data_size = VideoDecode::jtoolUtils.charLen;
    byteBufWrite(VideoDecode::byteBuf, ((char *) dataBuff), data_size);
    free(dataBuff);
    VideoDecode::totalCatch++;
    if (VideoDecode::totalCatch > 10) {
        VideoDecode::totalCatch = 0;
        VideoDecode::totalCatch = 0;
        data_size = VideoDecode::byteBuf->writeIndex - VideoDecode::byteBuf->readIndex;
        uint8_t *tmpData = (uint8_t *) malloc(data_size);
        byteBufRead(VideoDecode::byteBuf, (char *) tmpData, data_size);
        VideoDecode::totalCatch = 0;
        uint8_t *data = (uint8_t *) tmpData;
        int ret = 0;
        while (data_size > 0) {
            // 把原始数据封装到 pkt 中
            ret = av_parser_parse2(VideoDecode::parser, VideoDecode::c, &VideoDecode::pkt->data,
                                   &VideoDecode::pkt->size,
                                   data, data_size, AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
            if (ret < 0) {
                fprintf(stderr, "Error while parsing\n");
                return NULL;
            }
            data += ret;
            data_size -= ret;

            if (VideoDecode::pkt->size) {
                int z = VideoDecode::decode(VideoDecode::c, VideoDecode::frame, VideoDecode::pkt);
                if (z == 1) {
                    /* int totalSize = VideoDecode::c->width * VideoDecode::c->height * 3 / 2;
                     char * resultBuff = (char * )malloc(totalSize);
                     memcpy(resultBuff,VideoDecode::frame->data[0],totalSize);*/
                    VideoDecode::jtoolUtils.charLen = VideoDecode::rgbaBuffSize;
                    jbyteArray r = VideoDecode::jtoolUtils.charpoint2jarray(env,
                                                                            (char *) VideoDecode::rgbaFrame->data[0]);
                    // free(resultBuff);
                    return r;
                }
            }
        }
        free(tmpData);
    }

    return NULL;
}



extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecode_drawSurface(JNIEnv *env, jobject thiz, jobject m_surface,
                                                   jbyteArray bytes,int camera) {
    char *dataBuff = VideoDecode::jtoolUtils.jarray2charponit(env, bytes);
    int data_size = VideoDecode::jtoolUtils.charLen;
    byteBufWrite(VideoDecode::byteBuf, ((char *) dataBuff), data_size);
    free(dataBuff);
    VideoDecode::totalCatch++;
    if (VideoDecode::totalCatch > 3) {
        VideoDecode::totalCatch = 0;
        data_size = VideoDecode::byteBuf->writeIndex - VideoDecode::byteBuf->readIndex;
        uint8_t *tmpData = (uint8_t *) malloc(data_size);
        byteBufRead(VideoDecode::byteBuf, (char *) tmpData, data_size);
        uint8_t *data = (uint8_t *) tmpData;
        int ret = 0;
        while (data_size > 0) {
            // 解析数据包，其实就是封装 pkt
            ret = av_parser_parse2(VideoDecode::parser, VideoDecode::c, &VideoDecode::pkt->data,
                                   &VideoDecode::pkt->size,
                                   data, data_size, AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
            if (ret < 0) {
                fprintf(stderr, "Error while parsing\n");
                return;
            }
            data += ret;
            data_size -= ret;

            if (VideoDecode::pkt->size) {
                int z = VideoDecode::decode(VideoDecode::c, VideoDecode::frame, VideoDecode::pkt);
                if (z == 1) {
                    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, m_surface);
                    ANativeWindow_Buffer windowBuffer;
                    // get video width , height
                    int videoWidth = VideoDecode::c->width;
                    int videoHeight = VideoDecode::c->height;
                    int is90 = 1;
                    if(is90 == 1){
                        // 图像要经过一个旋转 640 * 480 旋转 90° 变成 480 * 640
                         videoWidth = VideoDecode::c->height;
                         videoHeight = VideoDecode::c->width;
                    }


                    // 设置native window的buffer大小,可自动拉伸
                    ALOGI("set native window");
                    ANativeWindow_setBuffersGeometry(nativeWindow, videoWidth, videoHeight,
                                                     WINDOW_FORMAT_RGBA_8888);
                    // lock native window buffer
                    // 锁定窗口缓存
                    ANativeWindow_lock(nativeWindow, &windowBuffer, NULL);
                    // 获取stride  绘制图像
                    uint8_t *dst = (uint8_t *) windowBuffer.bits;
                    // 此处拿到的frame是经过转换的
                    uint8_t *src = NULL;
                    if(is90 == 1){
                        //uint8_t  *tmpRotate =  (uint8_t *)VideoDecode::rotate90Clockwise(VideoDecode::defalutWidth,VideoDecode::defaultHeight);
                        // 逆时针
                        uint8_t  *tmpRotate = NULL;
                        if(camera == 0){
                            tmpRotate =  (uint8_t *)VideoDecode::rotate90Clockwise(VideoDecode::defalutWidth,VideoDecode::defaultHeight);
                        }else{
                            tmpRotate =  (uint8_t *)VideoDecode::rotate90Anticlockwise(VideoDecode::defalutWidth,VideoDecode::defaultHeight);
                        }
                        src = tmpRotate;
                        int dstStride = windowBuffer.stride * 4;
                        // 步幅由 480 * 4 转成 640 * 4;
                        int srcStride = videoWidth * 4;
                        // 由于window的stride和帧的stride不同,因此需要逐行复制
                        for (int i = 0; i < videoHeight; i++) {
                            memcpy(dst + i * dstStride, src + i * srcStride, srcStride);
                        }
                        free(tmpRotate);
                    }else{
                        src = VideoDecode::rgbaFrame->data[0];
                        int dstStride = windowBuffer.stride * 4;
                        int srcStride = VideoDecode::rgbaFrame->linesize[0];
                        for (int i = 0; i < videoHeight; i++) {
                            memcpy(dst + i * dstStride, src + i * srcStride, srcStride);
                        }
                    }


                    ANativeWindow_unlockAndPost(nativeWindow);
                    ANativeWindow_release(nativeWindow);
                    continue;
                }
            }
        }
        free(tmpData);
        clearByteBuf(VideoDecode::byteBuf);
    }
    return;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecode_freeContext(JNIEnv *env, jobject thiz) {
    if (VideoDecode::imgConvertCtx != NULL) {
        sws_freeContext(VideoDecode::imgConvertCtx);
    }
    freeByteBuf(VideoDecode::byteBuf);
    free(VideoDecode::buff);
    av_parser_close(VideoDecode::parser);
    avcodec_free_context(&VideoDecode::c);
    av_frame_free(&VideoDecode::frame);
    av_frame_free(&VideoDecode::rgbaFrame);
    av_packet_free(&VideoDecode::pkt);
}