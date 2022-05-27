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

struct DecodeContxt {
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
    AVPixelFormat converFormat ;
    ByteBuf *byteBuf;
    int totalCatch = 0;
};

char * rotate90Clockwise(int width, int height, DecodeContxt * context) {
    char *resultBuff = (char *) malloc(context->rgbaBuffSize);
    int x = 0;
    int y = 0;
    int posR = 0;
    int posS = 0;
    for (x = 0; x < width; x++) {
        for (y = height - 1; y >= 0; y--) {
            posS = (y * width + x) * 4;
            resultBuff[posR + 0] = context->rgbaFrame->data[0][posS + 0];// R
            resultBuff[posR + 1] = context->rgbaFrame->data[0][posS + 1];// G
            resultBuff[posR + 2] = context->rgbaFrame->data[0][posS + 2];// B
            resultBuff[posR + 3] = context->rgbaFrame->data[0][posS + 3];// A
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
char * rotate90Anticlockwise(int width, int height,DecodeContxt * context) {
    char *resultBuff = (char *) malloc(context->rgbaBuffSize);
    int x = 0;
    int y = 0;
    int posR = 0;
    int posS = 0;
    for (x = width-1; x >= 0; x--) {
        for (y = height - 1; y >= 0; y--) {
            posS = (y * width + x) * 4;
            resultBuff[posR + 0] = context->rgbaFrame->data[0][posS + 0];// R
            resultBuff[posR + 1] = context->rgbaFrame->data[0][posS + 1];// G
            resultBuff[posR + 2] = context->rgbaFrame->data[0][posS + 2];// B
            resultBuff[posR + 3] = context->rgbaFrame->data[0][posS + 3];// A
            posR += 4;
        }
    }
    return resultBuff;
}

int decode(AVCodecContext *dec_ctx, AVFrame *frame, AVPacket *pkt,DecodeContxt * context) {
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
        if (context->imgConvertCtx == NULL) {
            context->imgConvertCtx = sws_getContext(dec_ctx->width, dec_ctx->height,
                                                        dec_ctx->pix_fmt, dec_ctx->width,
                                                        dec_ctx->height,
                                                        context->converFormat, SWS_BICUBIC,
                                                        NULL, NULL, NULL);
        }
        // 转换数据 转换为目标 格式
        ret =  sws_scale(context->imgConvertCtx, frame->data, frame->linesize,
                  0, dec_ctx->height,
                  context->rgbaFrame->data, context->rgbaFrame->linesize);
        ALOGE("saving frame %3d\n", dec_ctx->frame_number);
        if(ret < context->defaultHeight){
            return  -1;
        }
        ALOGE("sws_scale %d\n", ret);
        fflush(stdout);
        return 1;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecodeObj_initContext(JNIEnv *env, jobject thiz) {


    struct DecodeContxt *montext = (DecodeContxt *) malloc(sizeof(struct DecodeContxt));


    montext->defalutWidth = 640;
    montext->defaultHeight = 480;
    montext->totalCatch = 0;
    montext->converFormat = AV_PIX_FMT_RGBA;
    montext->imgConvertCtx = NULL;

    montext->pkt = av_packet_alloc();
    if (!montext->pkt) {
        ALOGE("pkt malloc error");
        return;
    }

    montext->codec = avcodec_find_decoder(VIDEO_CODE);
    if (!montext->codec) {
        ALOGE("Codec not found\n");
        return;
    }

    montext->parser = av_parser_init(montext->codec->id);
    if (!montext->parser) {
        ALOGE("parser not found\n");
        return;
    }

    montext->c = avcodec_alloc_context3(montext->codec);
    if (!montext->c) {
        ALOGE("Could not allocate video codec context\n");
        return;
    }



    /* open it */
    if (avcodec_open2(montext->c, montext->codec, NULL) < 0) {
        ALOGE("Could not open codec\n");
        return;
    }

    montext->frame = av_frame_alloc();
    if (!montext->frame) {
        ALOGE("Could not allocate video frame\n");
        return;
    }
    montext->rgbaFrame = av_frame_alloc();
    if (!montext->rgbaFrame) {
        ALOGE("Could not allocate video frame\n");
        return;
    }


    montext->rgbaBuffSize = avpicture_get_size(montext->converFormat,
                                               montext->defalutWidth,
                                               montext->defaultHeight);
    montext->buff = (uint8_t *) av_malloc(montext->rgbaBuffSize * (sizeof(uint8_t)));
    // 填充帧内的数据 比如 linesize[]  和 data[]
    avpicture_fill((AVPicture *) montext->rgbaFrame, montext->buff,
                   montext->converFormat, montext->defalutWidth,
                   montext->defaultHeight);

    montext->byteBuf = newByteBuf();

    ALOGE("video decode init ok");


    jclass clazz = (jclass) env->GetObjectClass(thiz);
    jfieldID fid = (jfieldID) (*env).GetFieldID(clazz, "decodeContext", "J");
    env->SetLongField(thiz, fid, (jlong) montext);
}
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecodeObj_decodeFrame(JNIEnv *env, jobject thiz, jbyteArray bytes) {
    // TODO: implement decodeFrame()
}
extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecodeObj_freeContext(JNIEnv *env, jobject thiz) {
    jclass objClazz = (jclass) env->GetObjectClass(thiz);//obj为对应的JAVA对象
    jfieldID fid = env->GetFieldID(objClazz, "decodeContext", "J");
    jlong p = (jlong) env->GetLongField(thiz, fid);
    struct DecodeContxt *contxt = (DecodeContxt *) p;

    if (contxt->imgConvertCtx != NULL) {
        sws_freeContext(contxt->imgConvertCtx);
    }
    freeByteBuf(contxt->byteBuf);
    free(contxt->buff);
    av_parser_close(contxt->parser);
    avcodec_free_context(&(contxt->c));
    av_frame_free(&(contxt->frame));
    av_frame_free(&contxt->rgbaFrame);
    av_packet_free(&contxt->pkt);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_haojiangbo_ffmpeg_VideoDecodeObj_drawSurface(JNIEnv *env, jobject thiz, jobject m_surface,
                                                      jbyteArray bytes, jint camera) {
    jclass objClazz = (jclass) env->GetObjectClass(thiz);//obj为对应的JAVA对象
    jfieldID fid = env->GetFieldID(objClazz, "decodeContext", "J");
    jlong p = (jlong) env->GetLongField(thiz, fid);
    struct DecodeContxt *contxt = (DecodeContxt *) p;



    char *dataBuff = contxt->jtoolUtils.jarray2charponit(env, bytes);
    int data_size = contxt->jtoolUtils.charLen;
    byteBufWrite(contxt->byteBuf, ((char *) dataBuff), data_size);
    free(dataBuff);
    contxt->totalCatch++;
    if (contxt->totalCatch > 3) {
        contxt->totalCatch = 0;
        data_size = contxt->byteBuf->writeIndex - contxt->byteBuf->readIndex;
        uint8_t *tmpData = (uint8_t *) malloc(data_size);
        byteBufRead(contxt->byteBuf, (char *) tmpData, data_size);
        uint8_t *data = (uint8_t *) tmpData;
        int ret = 0;
        while (data_size > 0) {
            // 解析数据包，其实就是封装 pkt
            ret = av_parser_parse2(contxt->parser, contxt->c, &(contxt->pkt->data),
                                   &(contxt->pkt->size),
                                   data, data_size, AV_NOPTS_VALUE, AV_NOPTS_VALUE, 0);
            if (ret < 0) {
                fprintf(stderr, "Error while parsing\n");
                return;
            }
            data += ret;
            data_size -= ret;

            if (contxt->pkt->size) {
                int z =decode(contxt->c, contxt->frame, contxt->pkt,contxt);
                if (z == 1) {
                    ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, m_surface);
                    ANativeWindow_Buffer windowBuffer;
                    // get video width , height
                    int videoWidth = contxt->c->width;
                    int videoHeight = contxt->c->height;
                    int is90 = 1;
                    if(is90 == 1){
                        // 图像要经过一个旋转 640 * 480 旋转 90° 变成 480 * 640
                        videoWidth = contxt->c->height;
                        videoHeight = contxt->c->width;
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
                            tmpRotate =  (uint8_t *)rotate90Clockwise(contxt->defalutWidth,contxt->defaultHeight,contxt);
                        }else{
                            tmpRotate =  (uint8_t *)rotate90Anticlockwise(contxt->defalutWidth,contxt->defaultHeight,contxt);
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
                        src = contxt->rgbaFrame->data[0];
                        int dstStride = windowBuffer.stride * 4;
                        int srcStride = contxt->rgbaFrame->linesize[0];
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
        clearByteBuf(contxt->byteBuf);
    }
    return;



}