package com.haojiangbo.hander;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.utils.SessionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
  * 心跳包
 　　* @author 郝江波
 　　* @date 2020/4/17 10:47
 　　*/
 @Slf4j
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
            ctx.channel().close();
        }
        super.channelIdle(ctx, evt);
    }

    public static void sendPingMessage(Channel channel) {
        String messgae = "ping";
        String sessionId = SessionUtils.genSessionId(ClientConfig.INSTAND.getClientId());
        ByteBuf byteBuf =  Unpooled.wrappedBuffer(messgae.getBytes());
        channel.writeAndFlush(new CustomProtocol(
                ConstantValue.PING,
                sessionId.getBytes().length,
                sessionId,
                byteBuf.readableBytes(),byteBuf));
    }
}
