package com.haojiangbo.net.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * 该协议使用UDP传输，
 * 不考虑丢包情况
 * 也不会重新传输
 * @author Administrator
 * @version V1.0
 * @Title: MediaDataProtocol
 * @Package com.haojiangbo.protocol
 * @Description: 多媒体数据传输协议
 * @date 2020/11/19
 */
public class MediaDataProtocol {
    // 心跳
    public static final byte PING = 1;
    // 语音数据
    public static final byte AUDIO_DATA = 2;
    // 视频数据
    public static final byte VIDEO_DATA = 3;
    // 协议类型
    public byte type;
    // 电话号码
    public byte[] number;
    // 方便以后扩展的字段
    public int dataSize;
    // 数据负载
    public byte[] data;


    public static MediaDataProtocol byteBufToMediaDataProtocol(ByteBuf byteBuf){
        MediaDataProtocol protocol = new MediaDataProtocol();
        protocol.type = byteBuf.readByte();
        byte [] number = new byte[6];
        byteBuf.readBytes(number);
        protocol.number = number;
        protocol.dataSize = byteBuf.readInt();
        byte data[] = new byte[protocol.dataSize];
        byteBuf.readBytes(data);
        protocol.data = data;
        return  protocol;
    }


    public static ByteBuf mediaDataProtocolToByteBuf(Channel channel, MediaDataProtocol mediaDataProtocol){
        int totalLen = 11;
        totalLen += mediaDataProtocol.data.length;
        ByteBuf byteBuf =  channel.alloc().buffer(totalLen);
        byteBuf.writeByte(mediaDataProtocol.type);
        byteBuf.writeBytes(mediaDataProtocol.number);
        byteBuf.writeInt(mediaDataProtocol.dataSize);
        byteBuf.writeBytes(mediaDataProtocol.data);
        return  byteBuf;
    }

}
