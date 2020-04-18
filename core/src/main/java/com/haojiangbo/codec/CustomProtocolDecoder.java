package com.haojiangbo.codec;

import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 解码器
 * <pre>
 * 自己定义的协议
 *  数据包格式
 * +——----——+——-----——+——----——+
 * |协议开始标志|  长度             |   数据       |
 * +——----——+——-----——+——----——+
 * 1.协议开始标志head_data，为int类型的数据，16进制表示为0X76
 * 2.传输数据的长度contentLength，int类型
 * 3.要传输的数据,长度不应该超过2048，防止socket流的攻击
 * </pre>
 */
public class CustomProtocolDecoder extends ByteToMessageDecoder {

    /**
     * <pre>
     * 协议开始的标准head_data，int类型，占据4个字节.
     * 表示数据的长度contentLength，int类型，占据4个字节.
     * </pre>
     */
    public final static int BASE_LENGTH = 4 + 4 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer,
                          List<Object> out) throws Exception {
        // 可读长度必须大于基本长度
        if (buffer.readableBytes() >= BASE_LENGTH) {

            // 记录包头开始的index
            int beginReader;

            while (true) {
                // 获取包头开始的index
                beginReader = buffer.readerIndex();
                // 标记包头开始的index
                buffer.markReaderIndex();
                // 读到了协议的开始标志，结束while循环
                if (buffer.readInt() == ConstantValue.HEAD_DATA) {
                    break;
                }

                // 未读到包头，略过一个字节
                // 每次略过，一个字节，去读取，包头信息的开始标记
                buffer.resetReaderIndex();
                buffer.readByte();

                // 当略过，一个字节之后，
                // 数据包的长度，又变得不满足
                // 此时，应该结束。等待后面的数据到达
                if (buffer.readableBytes() < BASE_LENGTH) {
                    return;
                }
            }
            // 消息类型
            int messageType = buffer.readInt();


            // 会话长度
            int sesstionIdLength = buffer.readInt();
            if (buffer.readableBytes() < sesstionIdLength) {
                // 还原读指针
                buffer.readerIndex(beginReader);
                return;
            }
            // 会话ID
            byte[] sessionId = new byte[sesstionIdLength];
            buffer.readBytes(sessionId);


            // 消息的长度
            int length = buffer.readInt();
            // 判断请求数据包数据是否到齐
            if (buffer.readableBytes() < length) {
                // 还原读指针
                buffer.readerIndex(beginReader);
                return;
            }
            // 读取data数据 使用堆外内存
            ByteBuf byteBuf = Unpooled.directBuffer(length);
            buffer.readBytes(byteBuf);


            // 组装消息
            out.add(new CustomProtocol(
                    messageType,
                    sesstionIdLength,
                    new String(sessionId),
                    byteBuf.capacity(),
                    byteBuf));
        }
    }

}
