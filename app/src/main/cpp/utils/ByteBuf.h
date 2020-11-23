//
// Created by Administrator on 2020/11/23.
//

#ifndef NDKDEMO_BYTEBUF_H
#define NDKDEMO_BYTEBUF_H
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
/**
 *
 * 参考netty的ByteBuf使用方法
 * 实现的
 * 原先是C语言版本的
 * 在内网穿透的项目里
 * 又挪过来了  - -!
 * @anchor 郝江波
 *
 */
struct ByteBuf {
    int readIndex;
    int writeIndex;
    char *buff;
    int markerIndex;
    int capacity;
};
struct ByteBuf * newByteBuf();
void byteBufWrite (struct ByteBuf *buf,char *data,size_t size);
void byteBufRead (struct ByteBuf *buf,char *data,size_t size);
int markerIndex (struct ByteBuf *buf);
void resetIndex (struct ByteBuf *buf);
void doResetIndex (struct ByteBuf *buf,int markerIndex);
void freeByteBuf(struct ByteBuf *buf);
void clearByteBuf(struct ByteBuf *buf);
int byteBufReadInt(struct ByteBuf *buf);
char byteBufReadByte(struct ByteBuf *buf);
#endif //NDKDEMO_BYTEBUF_H
