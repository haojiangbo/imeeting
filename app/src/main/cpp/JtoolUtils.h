//
// Created by Administrator on 2020/11/19.
//

#ifndef NDKDEMO_JTOOLUTILS_H
#define NDKDEMO_JTOOLUTILS_H
#include <jni.h>
#include <stdio.h>

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


class JtoolUtils {
public:
    int charLen;
    char *jarray2charponit(JNIEnv *env,jbyteArray array);
    jbyteArray  charpoint2jarray(JNIEnv *env,char *data);
};


#endif //NDKDEMO_JTOOLUTILS_H
