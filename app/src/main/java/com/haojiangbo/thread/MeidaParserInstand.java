package com.haojiangbo.thread;

import android.util.Log;

import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.ffmpeg.AudioDecode;
import com.haojiangbo.ffmpeg.AudioDecodeObj;
import com.haojiangbo.list.apder.UserInfoModel;
import com.haojiangbo.ndkdemo.MettingActivite;
import com.haojiangbo.net.protocol.MediaDataProtocol;
import com.haojiangbo.net.tcp.ControlProtocolManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class MeidaParserInstand implements Runnable {
    /**
     * 数据队列
     */
    public static final BlockingDeque<ByteBuf> MEDIA_DATA_QUEUE = new LinkedBlockingDeque<>();


    public static final Map<String, AudioDecodeObj> AUDIO_DECODE_OBJ_MAP = new HashMap<>();


    public static volatile int THREAD_STATE = 1;

    @Override
    public void run() {
        while (THREAD_STATE == 1) {
            try {
                ByteBuf byteBuf = MEDIA_DATA_QUEUE.take();
                if (byteBuf.readableBytes() > 0) {
                    byte[] number = new byte[14];
                    byteBuf.readBytes(number);
                    String session = new String(number);
                    byteBuf.readBytes(4);
                    byte[] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bytes);
                    AudioDecodeObj decode = AUDIO_DECODE_OBJ_MAP.get(session);
                    if (null == decode) {
                        decode = new AudioDecodeObj();
                        decode.initContext();
                        AUDIO_DECODE_OBJ_MAP.put(session, decode);
                    }
                  /* // 转换协议
                   MediaDataProtocol protocol =   MediaDataProtocol.byteBufToMediaDataProtocol(byteBuf);*/
                    Log.i("负载大小>>", bytes.length + ">>>>");
                    byte[] converData = decode.decodeFrame(bytes);
                    UserInfoModel model = MettingActivite.VIDEO_CACHE.get(session);
                    if (converData != null && converData.length > 0 && null != model && !session.equals(ControlProtocolManager.getSessionId()) && THREAD_STATE == 1) {
                        Log.i("解析后的数据", ">>>>>" + converData.length);
                        model.getAudioTrackManager().startPlay(converData);
                    }
                    ReferenceCountUtil.release(byteBuf);
                }
            } catch (Exception e) {
                THREAD_STATE = 0;
                e.printStackTrace();
            }
        }
    }

    public static void freeContext() {
        MEDIA_DATA_QUEUE.clear();
        Set<String> stringSet = AUDIO_DECODE_OBJ_MAP.keySet();
        for (String key : stringSet) {
            AUDIO_DECODE_OBJ_MAP.get(key).freeContext();
        }
        AUDIO_DECODE_OBJ_MAP.clear();
        THREAD_STATE = 0;
    }
}
