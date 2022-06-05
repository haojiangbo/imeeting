package com.haojiangbo.protocol;
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
    // 方便以后扩展的字段 2020/11/20
    // datasize占用2个字节,低字节位2字节表示数据大小
    // 高2位字节 用来表示 摄像头的方向 前摄像头和后摄像头 2020/11/23
    // 协议设计这种，总是做的时候才能发现坑
    public int dataSize;
    // 数据负载
    public byte[] data;


    public static MediaDataProtocol byteBufToMediaDataProtocol(ByteBuf byteBuf) {
        MediaDataProtocol protocol = new MediaDataProtocol();
        protocol.type = byteBuf.readByte();
        byte[] number = new byte[14];
        byteBuf.readBytes(number);
        protocol.number = number;
        // 此处要修改一下，
        protocol.dataSize = byteBuf.readInt();
        // 取出 int 2 个 低字节 得到数据总大小
        int dataSize =
                ((protocol.dataSize & 0xffff) >>> 8) << 8
                        |
                        (protocol.dataSize & 0xff);
        byte data[] = new byte[dataSize];
        byteBuf.readBytes(data);
        protocol.data = data;
        return protocol;
    }


    public static ByteBuf mediaDataProtocolToByteBuf(Channel channel, MediaDataProtocol mediaDataProtocol) {
        int totalLen = 19;
        totalLen += mediaDataProtocol.data.length;
        ByteBuf byteBuf = channel.alloc().buffer(totalLen);
        byteBuf.writeByte(mediaDataProtocol.type);
        byteBuf.writeBytes(mediaDataProtocol.number);
        byteBuf.writeInt(mediaDataProtocol.dataSize);
        byteBuf.writeBytes(mediaDataProtocol.data);
        return byteBuf;
    }

}
