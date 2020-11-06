package com.haojiangbo.protocol.codec;

import com.haojiangbo.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlProtocolWriteLog extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(stringBuilder, (ByteBuf) msg);
        log.info("\n 返回数据 \n = {}", stringBuilder.toString());
        super.write(ctx, msg, promise);
    }
}
