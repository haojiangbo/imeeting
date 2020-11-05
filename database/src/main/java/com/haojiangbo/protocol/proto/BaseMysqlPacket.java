package com.haojiangbo.protocol.proto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
* @Title: BaseMysqlPacket
* @Package com.haojiangbo.protocol.proto
* @Description: 基础数据包
* @author 郝江波
* @date 2020/11/3
* @version V1.0
*/
public class BaseMysqlPacket {
    /**
     * 管道上下文
     */
    public ChannelHandlerContext context;
    /**
     * 此处只有3位
     */
    public int packerLength;
    /**
     *  1个字节的自增序列
     */
    public byte packetNumber;
    /**
     * 负载数据
     */
    public ByteBuf payload;
}
