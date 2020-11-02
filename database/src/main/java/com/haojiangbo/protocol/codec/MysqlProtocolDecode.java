package com.haojiangbo.protocol.codec;

import com.haojiangbo.utils.ByteUtilBigLittle;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

/**
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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf byteBuf =   genMysqlInitialHandshakePacket();
        ctx.channel().writeAndFlush(byteBuf);
        super.channelActive(ctx);
    }

    /**
     * 测试代码 封装握手协议
     * @return
     */
    ByteBuf  genMysqlInitialHandshakePacket(){
        int i = 0;
        // 协议版本
        byte protocolVersion = 0x0a;
        i += 1;
        // 版本号
        String serverVersion = "5.6.45";
        byte filler = 0x00;
        i += (serverVersion.getBytes().length + 1);
        // 线程ID
        ByteBuffer threadId = ByteBuffer.allocate(4);
        threadId.order(ByteOrder.LITTLE_ENDIAN);
        threadId.asIntBuffer().put(6);
        byte [] threadIdByte =  threadId.array();
        i += 4;
        // 颜
        String salt = "JDTZG3h7";
        // C中的 \0 代表字符串末尾
        i += (salt.getBytes().length + 1);

        // 能力 此处参考的是抓包的值
        byte [] capabilityFalg = new byte[]{(byte) 0xff,(byte) 0xf7};
        i += capabilityFalg.length;
        byte charSet  = (byte) 0xe0;
        i += 1;
        byte [] serverStaus = new byte[]{(byte) 0x02, (byte) 0x00};
        i += serverStaus.length;
        byte [] capabilityFlags = new byte[]{ (byte) 0x7f,(byte) 0x80};
        i += capabilityFlags.length;
        int auth_plugin_data_len;
        i += 1;
        String auth_plugin_name = "mysql_native_password";
        auth_plugin_data_len =  auth_plugin_name.getBytes().length;
        // C中的 \0 代表字符串末尾
        i += auth_plugin_data_len + 1;

        int size = i+4;
        ByteBuf result = Unpooled.buffer(size);
        byte [] len = ByteUtilBigLittle.intToByteLittle(i);
        // 此处的长度 不是指整个协议包的长度 而是指的数据长度
        result.writeBytes(len,0,len.length-1);
        // 序列号 此处要自增
        result.writeByte(0);
        // 协议号
        result.writeByte(protocolVersion);

        result.writeBytes(serverVersion.getBytes());
        result.writeByte(filler);

        result.writeBytes(threadIdByte);

        result.writeBytes(salt.getBytes());
        result.writeByte(filler);

        result.writeBytes(capabilityFalg);
        result.writeByte(charSet);
        result.writeBytes(serverStaus);
        result.writeBytes(capabilityFlags);
        result.writeByte(ByteUtilBigLittle.intToByteLittle(auth_plugin_data_len)[0]);

        result.writeBytes(auth_plugin_name.getBytes());
        result.writeByte(filler);
        return result;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte [] dataLenArray = new byte[4];
        byteBuf.readBytes(dataLenArray,0,3);
        int dataLen =  ByteUtilBigLittle.bytes2IntLittle(dataLenArray);
        log.info("协议数据长度  = {}",dataLen);
        throw  new RuntimeException("未完成的代码");
    }
}
