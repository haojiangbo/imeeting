package com.haojiangbo.hander;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.config.SessionChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientProxyHander extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        ByteBuf result = byteBuf.duplicate();
        String sessionId = ctx.channel().attr(NettyProxyMappingConstant.SESSION).get();
        log.info("交换数据 === byte length {}", byteBuf.readableBytes());

        target.writeAndFlush(new CustomProtocol(
                ConstantValue.DATA,
                ClientConfig.INSTAND.getClientId(),
                sessionId.getBytes().length,
                sessionId,
                result.readableBytes(),
                result
        ));

        // 释放引用
        //ReferenceCountUtil.release(byteBuf);

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String sessionId = ctx.channel().attr(NettyProxyMappingConstant.SESSION).get();
        SessionChannelMapping.SESSION_CHANNEL_MAPPING.remove(sessionId);
        ctx.channel().attr(NettyProxyMappingConstant.MAPPING).set(null);
        super.channelInactive(ctx);
    }
}
