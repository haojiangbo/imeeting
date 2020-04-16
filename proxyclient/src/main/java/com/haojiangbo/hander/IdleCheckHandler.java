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
import sun.plugin.util.UIUtil;

import java.util.UUID;

/**
 * check idle chanel.
 *
 * @author fengfei
 *
 */
public class IdleCheckHandler extends IdleStateHandler {

    public static final int USER_CHANNEL_READ_IDLE_TIME = 1200;

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
        String sessionId = SessionUtils.genSessionId(String.valueOf(ClientConfig.INSTAND.getClientId()));
        CustomProtocol msg = new CustomProtocol(
                ConstantValue.PING,
                ClientConfig.INSTAND.getClientId(),
                sessionId.getBytes().length,
                sessionId,
                messgae.getBytes().length,messgae.getBytes());
        channel.writeAndFlush(msg);
    }
}
