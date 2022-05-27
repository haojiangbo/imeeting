package com.haojiangbo.thread;

import android.util.Log;

import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.ffmpeg.AudioDecode;
import com.haojiangbo.ndkdemo.MettingActivite;
import com.haojiangbo.net.protocol.MediaDataProtocol;
import com.haojiangbo.net.tcp.ControlProtocolManager;

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
        while (i == 1) {
            try {
               ByteBuf byteBuf =  MEDIA_DATA_QUEUE.take();
               if(byteBuf.readableBytes() > 0){
                   byte[] number = new byte[14];
                   byteBuf.readBytes(number);
                   String session = new String(number);
                   byteBuf.readBytes(4);
                   byte [] bytes = new byte[byteBuf.readableBytes()];
                   byteBuf.readBytes(bytes);
                  /* // 转换协议
                   MediaDataProtocol protocol =   MediaDataProtocol.byteBufToMediaDataProtocol(byteBuf);*/
                   Log.e("负载大小>>",bytes.length + ">>>>");
                   byte [] converData =  decode.decodeFrame(bytes);
                   AudioTrackManager audioTrackManager = MettingActivite.audioManager.get(session);
                   if(converData != null && null != audioTrackManager && !session.equals(ControlProtocolManager.getSessionId())){
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
        decode.freeContext();
    }
}
