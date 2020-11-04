package com.haojiangbo.protocol.codec;

import com.haojiangbo.protocol.config.CommandTypeFlag;
import com.haojiangbo.protocol.config.ErrorCode;
import com.haojiangbo.protocol.parser.QueryCommandParser;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.ErrorPacket;
import com.haojiangbo.protocol.proto.OkPackert;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
* @Title: MysqlCommandHandler
* @Package com.haojiangbo.protocol.codec
* @Description: 命令读取
* @author 郝江波
* @date 2020/11/3
* @version V1.0
*/
@Slf4j
public class MysqlCommandHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BaseMysqlPacket baseMysqlPacket = (BaseMysqlPacket) msg;
        StringBuilder str = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(str,((BaseMysqlPacket) msg).payload);
        log.info("\n 客户端发来的命令 \n {}",str);
        OkPackert okPackert = new OkPackert();
        switch (baseMysqlPacket.payload.readByte()) {
            case CommandTypeFlag.COM_INIT_DB:
                okPackert.success(ctx, (byte) (baseMysqlPacket.packetNumber + 1));
                break;
            case CommandTypeFlag.COM_QUERY:
                QueryCommandParser queryCommandParser = new QueryCommandParser();
                queryCommandParser.parder(ctx,baseMysqlPacket);
                break;
            case CommandTypeFlag.COM_PING:
                okPackert.success(ctx, (byte) (baseMysqlPacket.packetNumber + 1));
                break;
            default:
                ErrorPacket err = new ErrorPacket();
                err.errcode = ErrorCode.ER_UNKNOWN_COM_ERROR;
                err.message = "Unknown command";
                err.write(ctx.channel());
                break;
        }
    }


}

/* case CommandTypeFlag.COM_STMT_PREPARE:
                // todo prepare支持,参考MyCat
                source.stmtPrepare(bin.data);
                break;
            case CommandTypeFlag.COM_STMT_EXECUTE:
                source.stmtExecute(bin.data);
                break;
            case CommandTypeFlag.COM_STMT_CLOSE:
                source.stmtClose(bin.data);
                break;
            case CommandTypeFlag.COM_HEARTBEAT:
                source.heartbeat(bin.data);
                break;
           case CommandTypeFlag.COM_QUIT:
                break;

                */