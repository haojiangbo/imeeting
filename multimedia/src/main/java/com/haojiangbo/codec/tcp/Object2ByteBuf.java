package com.haojiangbo.codec.tcp;


import com.haojiangbo.protocol.ControlProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class Object2ByteBuf {
    public static ByteBuf obj2buf(Channel channel, ControlProtocol controlProtocol){
        int totalSize = 3;
        totalSize += controlProtocol.sessionSize;
        totalSize += 4;
        totalSize += controlProtocol.dataSize;
        ByteBuf byteBuf =  channel.alloc().buffer(totalSize);
        byteBuf.writeByte(controlProtocol.header);
        byteBuf.writeByte(controlProtocol.flag);
        byteBuf.writeByte(controlProtocol.sessionSize);
        byteBuf.writeBytes(controlProtocol.session);
        byteBuf.writeInt(controlProtocol.dataSize);
        byteBuf.writeBytes(controlProtocol.data);
        return  byteBuf;
    }
}
