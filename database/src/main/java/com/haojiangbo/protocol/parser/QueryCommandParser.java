package com.haojiangbo.protocol.parser;

import com.haojiangbo.protocol.config.ErrorCode;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.ErrorPacket;
import com.haojiangbo.protocol.proto.OkPackert;
import com.haojiangbo.protocol.proto.ResultSetPacket;
import com.haojiangbo.router.SQLRouter;
import com.haojiangbo.thread.DataBaseRuntimeThreadPool;
import com.haojiangbo.thread.RuntimeInstance;
import com.haojiangbo.utils.PathUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class QueryCommandParser extends CommonCommandParser {


    @Override
    public void parder(ChannelHandlerContext ctx, BaseMysqlPacket baseMysqlPacket) {
        DataBaseRuntimeThreadPool.exec(new RuntimeInstance(baseMysqlPacket));
    }
}
