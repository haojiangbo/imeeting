package com.haojiangbo.protocol.codec;

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
   public static final AttributeKey<byte[]> PASSWORDPART = AttributeKey.newInstance("PASSWORDPART");

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
       /* ByteBuf byteBuf =   genMysqlInitialHandshakePacket();
        ctx.channel().writeAndFlush(byteBuf);*/
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.serverVersion = "5.6.45";
        handshakePacket.authPluginDataPart1 = RandomUtil.randomBytes(8);
        handshakePacket.authPluginDataPart2 = RandomUtil.randomBytes(12);

        // 保存认证数据
        byte[] seed = new byte[handshakePacket.authPluginDataPart1.length + handshakePacket.authPluginDataPart2.length];
        System.arraycopy(handshakePacket.authPluginDataPart1, 0, seed, 0, handshakePacket.authPluginDataPart1.length);
        System.arraycopy(handshakePacket.authPluginDataPart2, 0, seed, handshakePacket.authPluginDataPart1.length, handshakePacket.authPluginDataPart2.length);
        ctx.channel().attr(PASSWORDPART).set(seed);
        log.info("seed1 == {}", Arrays.toString(seed));
        handshakePacket.write(ctx.channel());
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

        //填充值
        i += 10;

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
        byte [] tc = new byte[10];
        result.writeBytes(tc);
        result.writeBytes(auth_plugin_name.getBytes());
        result.writeByte(filler);
        return result;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        StringBuilder str = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(str,byteBuf);
        log.info("协议 = {} \n",str);
        if(byteBuf.readableBytes() >= 4){
            byteBuf.markReaderIndex();
            byte [] dataLenArray = new byte[4];
            byteBuf.readBytes(dataLenArray,0,3);
            // 数据总长度
            int dataLen =  ByteUtilBigLittle.bytes2IntLittle(dataLenArray);
            // 包的序号
            int packetNumber = byteBuf.readByte();

            // 长度不足 直接return
            if(byteBuf.readableBytes() < dataLen){
                byteBuf.resetReaderIndex();
                return;
            }
            byte [] tmp_capability_and_extend = new byte[4];
            byteBuf.readBytes(tmp_capability_and_extend);
            // 能力以及扩展
            int  capability_and_extend = ByteUtilBigLittle.bytes2IntLittle(tmp_capability_and_extend);
            dataLen -= 4;
            //最大包大小 统一小端解析
            ByteBuf maxPacket = byteBuf.readBytes(4);
            dataLen -= 4;
            // 编码集 用于解析下面4字节的 字符串
            byte charSet = byteBuf.readByte();
            dataLen -= 1;
            // 23 填充值
            ByteBuf filler = byteBuf.readBytes(23);
            dataLen -= 23;

            //账号
            int tmpIndex = 1;
            byteBuf.markReaderIndex();
            byte tmpValue = byteBuf.readByte();
            while (tmpValue != 0x00){
                tmpIndex ++;
                tmpValue = byteBuf.readByte();
            }
            byteBuf.resetReaderIndex();
            ByteBuf userName =  byteBuf.readBytes(tmpIndex - 1);
            // 跳过字符串结尾
            byteBuf.readByte();
            dataLen -= tmpIndex;
            log.info("账号 = {} 剩余长度 = {}",userName.toString(Charset.forName("utf-8")),dataLen);

            int pwdLen = byteBuf.readByte();
            dataLen -= 1;
            byte[] passwordArray = new byte[pwdLen];
            byteBuf.readBytes(passwordArray);
            dataLen -= pwdLen;

            log.info("密码 = {} 剩余长度 = {}",new String(passwordArray),dataLen);

            byte [] seed =  ctx.channel().attr(PASSWORDPART).get();
            log.info("seed2 == {}", Arrays.toString(seed));

            byte [] tmp = SecurityUtil.scramble411("root".getBytes(),seed);
            log.info("tmp == {}", Arrays.toString(tmp));
            log.info("pwd == {}", Arrays.toString(passwordArray));
            //return;
        }
    }
}
