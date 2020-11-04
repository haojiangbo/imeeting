package com.haojiangbo.protocol.codec;

import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.HandshakePacket;
import com.haojiangbo.utils.ByteUtilBigLittle;
import com.haojiangbo.utils.RandomUtil;
import com.haojiangbo.utils.SecurityUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * 参考文档
 * https://dev.mysql.com/doc/internals/en/initial-handshake.html
*
 * @Title: MysqlProtocolDecode
* @Package com.haojiangbo.protocol.codec
* @Description: mysql协议解析
* @author 郝江波
* @date 2020/10/29
* @version V1.0
*/
@Slf4j
public class MysqlProtocolDecode extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        StringBuilder str = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(str,byteBuf);
        log.info("\n 客户端数据 \n {} ",str);
        if(byteBuf.readableBytes() >= 4){
            byteBuf.markReaderIndex();
            // 数据总长度
            int byte1 = byteBuf.readByte() & 0xff;
            int byte2 = byteBuf.readByte() << 8;
            int byte3 = byteBuf.readByte() << 16;
            int dataLen =  byte1|byte2|byte3;

            // 长度不足 直接 return
            if(byteBuf.readableBytes() < dataLen){
                byteBuf.resetReaderIndex();
                return;
            }

            //封装基础协议
            BaseMysqlPacket baseMysqlPacket = new BaseMysqlPacket();
            baseMysqlPacket.packerLength = dataLen;
            baseMysqlPacket.packetNumber = byteBuf.readByte();
            baseMysqlPacket.payload = byteBuf.readBytes(dataLen);

            list.add(baseMysqlPacket);
        }
    }


}
