//
// Created by Administrator on 2020/11/23.
//
#include "ByteBuf.h"
struct ByteBuf * newByteBuf(){
    struct ByteBuf *buf = (ByteBuf *)malloc(sizeof(struct ByteBuf));
    buf->capacity = sizeof(char) * 1024 * 512;
    buf->buff = (char*)malloc(buf->capacity);
    buf->readIndex = 0;
    buf->writeIndex = 0;
    buf->markerIndex = 0;
    return  buf;
}

void dilatation(struct ByteBuf *buf, size_t newSize){
    char *tmp = buf->buff;
    char *newBuf  = (char *)malloc(buf->capacity + newSize);
    if(NULL == newBuf){
        perror("byteBuf dilatation error");
        fflush(stderr);
        exit(1);
    }
    memcpy(newBuf,tmp,buf->capacity);
    buf->buff = newBuf;
    free(tmp);
    buf->capacity += newSize;
}
void byteBufWrite (struct ByteBuf *buf,char *data,size_t size){
    if(buf->capacity - buf->writeIndex < size){
        dilatation(buf,size);
    }
    memcpy((buf->buff + buf->writeIndex),data,size);
    buf->writeIndex += size;
}

void byteBufRead (struct ByteBuf *buf,char *data,size_t size){
    size_t tmp = buf->writeIndex - buf->readIndex > size ? size : buf->writeIndex - buf->readIndex;
    memcpy(data,(buf->buff + buf->readIndex),tmp);
    buf->readIndex += tmp;
}

int byteBufReadInt(struct ByteBuf *buf){
    int result =  *((int *)(buf->buff + buf->readIndex));
    buf->readIndex += 4;
    return  result;
}

char byteBufReadByte(struct ByteBuf *buf){
    char  result =  *(buf->buff + buf->readIndex);
    buf->readIndex += 1;
    return result;
}

int  markerIndex (struct ByteBuf *buf){
    buf->markerIndex = buf->readIndex;
    return buf->readIndex;
}

void resetIndex (struct ByteBuf *buf){
    doResetIndex(buf,buf->markerIndex);
}

void doResetIndex (struct ByteBuf *buf,int markerIndex){
    buf->readIndex = markerIndex;
}


void clearByteBuf(struct ByteBuf *buf){
    if(buf->readIndex == 0 || buf->readIndex == buf->writeIndex){
        buf->readIndex = 0;
        buf->writeIndex = 0;
    }else{
        memcpy(buf->buff,buf->buff + buf->readIndex,buf->writeIndex - buf->readIndex);
        buf->writeIndex = buf->writeIndex - buf->readIndex;
        buf->readIndex = 0;
    }
}

void  freeByteBuf(struct ByteBuf *buf){
    free(buf->buff);
    free(buf);
}
