package com.haojiangbo.protocol.parser;

import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import io.netty.channel.ChannelHandlerContext;

public abstract class CommonCommandParser {
    public abstract void  parder(ChannelHandlerContext ctx,BaseMysqlPacket baseMysqlPacket);
}
