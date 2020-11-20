package com.haojiangbo.net.codec.tcp;


import com.haojiangbo.net.protocol.ControlProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 媒体控制协议解析
 * 　　* @author 郝江波
 * 　　* @date 2020/11/20 14:53
 */
public class MediaProtocolDecode extends ByteToMessageDecoder {
    public static int BASE_SIZE = 3;

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < BASE_SIZE) {
            return;
        }
        // 标记读标记位
        byteBuf.markReaderIndex();

        // 读取byte
        if (byteBuf.readByte() != ControlProtocol.HEADER) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte flag =  byteBuf.readByte();
        byte sesisonLen =  byteBuf.readByte();
        // 此处+4个字节是为了照顾 dataSize的长度
        if(byteBuf.readableBytes() < sesisonLen + 4){
            byteBuf.resetReaderIndex();
            return;
        }
        byte [] session = new byte[sesisonLen];
        byteBuf.readBytes(session);
        int dataSize =  byteBuf.readInt();
        if(byteBuf.readableBytes() < dataSize){
            byteBuf.resetReaderIndex();
            return;
        }

        ControlProtocol controlProtocol =   new ControlProtocol();
        controlProtocol.flag = flag;
        controlProtocol.sessionSize = sesisonLen;
        controlProtocol.session = session;
        controlProtocol.dataSize = dataSize;
        if(dataSize > 0){
            byte [] data = new byte[dataSize];
            byteBuf.readBytes(data);
            controlProtocol.data = data;
        }
        list.add(controlProtocol);
    }
}
