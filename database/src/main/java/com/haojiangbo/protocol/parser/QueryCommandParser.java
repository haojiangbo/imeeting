package com.haojiangbo.protocol.parser;

import com.haojiangbo.protocol.config.ErrorCode;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.ErrorPacket;
import com.haojiangbo.protocol.proto.OkPackert;
import com.haojiangbo.protocol.proto.ResultSetPacket;
import com.haojiangbo.router.SQLRouter;
import com.haojiangbo.utils.PathUtils;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class QueryCommandParser extends CommonCommandParser {


    @Override
    public void parder(ChannelHandlerContext ctx, BaseMysqlPacket baseMysqlPacket) {
        String command = baseMysqlPacket.payload.toString(Charset.forName("utf-8"));
        log.info("QUERY == {}",command);
        SQLRouter.setDbPath("D:/work/springCloudOnline/stinger/proxy/");
        SQLRouter.router(command);
        if("SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA".equals(command)){
            ResultSetPacket resultSetPacket = new ResultSetPacket();
            resultSetPacket.write(ctx.channel(),baseMysqlPacket);
        } else if("SHOW STATUS".equals(command)){
            ErrorPacket err = new ErrorPacket();
            err.errcode = ErrorCode.ER_UNKNOWN_COM_ERROR;
            err.message = "Unknown command";
            err.write(ctx.channel());
        } else {
            OkPackert okPackert = new OkPackert();
            okPackert.success(ctx, (byte) (baseMysqlPacket.packetNumber + 1));
        }
    }
}
