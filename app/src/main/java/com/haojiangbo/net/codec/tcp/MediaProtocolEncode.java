package com.haojiangbo.net.codec.tcp;


import com.haojiangbo.net.protocol.ControlProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MediaProtocolEncode extends MessageToByteEncoder<ControlProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ControlProtocol controlProtocol, ByteBuf byteBuf) throws Exception {
        byteBuf.writeByte(controlProtocol.header);
        byteBuf.writeByte(controlProtocol.flag);
        byteBuf.writeByte(controlProtocol.sessionSize);
        byteBuf.writeBytes(controlProtocol.session);
        byteBuf.writeInt(controlProtocol.dataSize);
        byteBuf.writeBytes(controlProtocol.data);
    }
}
