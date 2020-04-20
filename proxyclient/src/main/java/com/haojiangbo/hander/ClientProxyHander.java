package com.haojiangbo.hander;

import com.haojiangbo.config.SessionChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
 /**
  * 客户端代理类
 　　* @author 郝江波
 　　* @date 2020/4/17 10:32
 　　*/
@Slf4j
public class ClientProxyHander extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        if(target  == null || !target.isActive()){
            ctx.close();
            ReferenceCountUtil.release(byteBuf);
            return;
        }
        String sessionId = ctx.channel().attr(NettyProxyMappingConstant.SESSION).get();
        log.info("交换数据 {} byte ", byteBuf.readableBytes());
        target.writeAndFlush(new CustomProtocol(
                ConstantValue.DATA,
                sessionId.getBytes().length,
                sessionId,
                byteBuf.readableBytes(),
                byteBuf
        ));
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String sessionId = ctx.channel().attr(NettyProxyMappingConstant.SESSION).get();
        SessionChannelMapping.SESSION_CHANNEL_MAPPING.remove(sessionId);
        ctx.channel().attr(NettyProxyMappingConstant.MAPPING).set(null);
        super.channelInactive(ctx);
    }
}
