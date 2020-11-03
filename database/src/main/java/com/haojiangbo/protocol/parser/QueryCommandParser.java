package com.haojiangbo.protocol.parser;

import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.OkPackert;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class QueryCommandParser extends CommonCommandParser {


    @Override
    public void parder(ChannelHandlerContext ctx, BaseMysqlPacket baseMysqlPacket) {
        log.info("QUERY == {}",baseMysqlPacket.payload.toString(Charset.forName("utf-8")));
        OkPackert okPackert = new OkPackert();
        okPackert.success(ctx, (byte) (baseMysqlPacket.packetNumber + 1));
    }
}
