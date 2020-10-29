package com.haojiangbo.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
* @Title: MysqlProtocolDecode
* @Package com.haojiangbo.protocol.codec
* @Description: mysql协议解析
* @author 郝江波
* @date 2020/10/29
* @version V1.0
*/
@Slf4j
public class MysqlProtocolDecode extends ByteToMessageDecoder {


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
    }
}
