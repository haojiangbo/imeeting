package com.haojiangbo.hander;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientProxyHander  extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        Channel target =  ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        byte [] result = ByteBufUtil.getBytes(byteBuf);
        CustomProtocol message = new CustomProtocol(ConstantValue.PING, ClientConfig.INSTAND.getClientId(), result.length,result);
        target.writeAndFlush(message);
        log.info("交换数据 === byte length {}",result.length);
    }
}
