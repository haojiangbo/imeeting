package com.haojiangbo.net.tcp.hander;


import android.text.TextUtils;
import android.util.Log;

import com.haojiangbo.ndkdemo.MainActivity;
import com.haojiangbo.net.codec.tcp.Object2ByteBuf;
import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.protocol.ControlProtocol;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;


/**
 * 心跳包
 　　* @author 郝江波
 　　* @date 2020/4/17 10:47
 　　*/

public class IdleCheckHandler extends IdleStateHandler {


    public static final int READ_IDLE_TIME = 30;

    public static final int WRITE_IDLE_TIME = 13;

    public IdleCheckHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (IdleStateEvent.FIRST_WRITER_IDLE_STATE_EVENT == evt) {
            sendPingMessage(ctx.channel());
        } else if (IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT == evt) {
            if(!ctx.channel().isActive()){
                ctx.channel().close();
            }
        }
        super.channelIdle(ctx, evt);
    }

    public static void sendPingMessage(Channel channel) {
        String messgae = "ping";
        String sessionId =  UUID.randomUUID().toString();
        if(!StringUtil.isNullOrEmpty(MainActivity.CALL_NUMBER)){
            messgae = MainActivity.CALL_NUMBER;
        }
        channel.writeAndFlush(new ControlProtocol(ControlProtocol.PING,
                (byte) (sessionId.getBytes().length & 0xFF),sessionId.getBytes(),messgae.getBytes().length,messgae.getBytes()));
        Log.e("net>>>" ,"ping>>"+messgae);
    }
}
