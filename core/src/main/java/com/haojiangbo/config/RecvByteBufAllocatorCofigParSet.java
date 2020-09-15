package com.haojiangbo.config;

import io.netty.channel.AdaptiveRecvByteBufAllocator;

import  io.netty.channel.socket.SocketChannel;
/**
* @Title: RecvByteBufAllocatorCofigParSet
* @Package com.haojiangbo.config
* @Description: 设置最小包大小，空间换时间的解决方案
* @author 郝江波
* @date 2020/9/11
* @version V1.0
*/
public class RecvByteBufAllocatorCofigParSet {
    public static void  set(SocketChannel ch){
        // ch.config().setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(1024*4,1024*10,65535));
    }
}
