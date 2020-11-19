package com.haojiangbo.thread;

import android.util.Log;

import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.ffmpeg.AudioDecode;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class MeidaParserInstand implements Runnable {
    /**
     * 数据队列
     */
    public static final BlockingDeque<ByteBuf> MEDIA_DATA_QUEUE = new LinkedBlockingDeque<>();




    @Override
    public void run() {
        AudioDecode decode = new AudioDecode();
        decode.initContext();
        int i = 1;
        AudioTrackManager audioTrackManager = AudioTrackManager.getInstance();
        while (i == 1) {
            try {
               ByteBuf byteBuf =  MEDIA_DATA_QUEUE.take();
               if(byteBuf.readableBytes() > 0){
                   byte [] bytes = new byte[byteBuf.readableBytes()];
                   byteBuf.readBytes(bytes);
                   byte [] converData =  decode.decodeFrame(bytes);
                   if(converData != null){
                       Log.e("解析后的数据",">>>>>"+converData.length);
                       audioTrackManager.startPlay(converData);
                   }
                   ReferenceCountUtil.release(byteBuf);
               }
            } catch (InterruptedException e) {
                i = 0;
                e.printStackTrace();
            }
        }
        audioTrackManager.stopPlay();
        decode.freeContext();
    }
}
