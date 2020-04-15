package com.haojiangbo.hander;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

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
            String messgae = "ping";
            CustomProtocol msg = new CustomProtocol(ConstantValue.PING,ClientConfig.INSTAND.getClientId(),messgae.getBytes().length,messgae.getBytes());
            //ByteBuf out = getByteBuf(messgae, msg);
            ctx.channel().writeAndFlush(msg);
        } else if (IdleStateEvent.FIRST_READER_IDLE_STATE_EVENT == evt) {
            ctx.channel().close();
        }
        super.channelIdle(ctx, evt);
    }
}
