package com.haojiangbo.protocol.codec;

import com.haojiangbo.protocol.proto.AbstratorMySqlPacket;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.HandshakePacket;
import com.haojiangbo.protocol.proto.OkPackert;
import com.haojiangbo.utils.ByteUtilBigLittle;
import com.haojiangbo.utils.RandomUtil;
import com.haojiangbo.utils.SecurityUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
@Slf4j
public class MysqlAuthHander extends ChannelInboundHandlerAdapter {
    public static final AttributeKey<byte[]> PASSWORDPART = AttributeKey.newInstance("PASSWORDPART");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.serverVersion = "5.6.45";
        handshakePacket.authPluginDataPart1 = RandomUtil.randomBytes(8);
        handshakePacket.authPluginDataPart2 = RandomUtil.randomBytes(12);

        // 保存认证数据
        byte[] seed = new byte[handshakePacket.authPluginDataPart1.length + handshakePacket.authPluginDataPart2.length];
        System.arraycopy(handshakePacket.authPluginDataPart1, 0, seed, 0, handshakePacket.authPluginDataPart1.length);
        System.arraycopy(handshakePacket.authPluginDataPart2, 0, seed, handshakePacket.authPluginDataPart1.length, handshakePacket.authPluginDataPart2.length);
        ctx.channel().attr(PASSWORDPART).set(seed);
        handshakePacket.write(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BaseMysqlPacket baseMysqlPacket = (BaseMysqlPacket) msg;
        // 读取协议
        readAuthProto(ctx,baseMysqlPacket);
        // 返回成功
        success(ctx);
    }

    private void success(ChannelHandlerContext ctx){
        // AUTH_OK , process command
        ctx.pipeline().replace(this, "mysqlCommandHandler", new MysqlCommandHandler());
        OkPackert okPackert = new OkPackert();
        okPackert.success(ctx);
    }

    /**
     * 读取握手协议
     * @param ctx
     * @param baseMysqlPacket
     * @throws NoSuchAlgorithmException
     */
    private boolean readAuthProto(ChannelHandlerContext ctx, BaseMysqlPacket baseMysqlPacket) throws NoSuchAlgorithmException {
        ByteBuf byteBuf = baseMysqlPacket.payload;
        byte [] tmp_capability_and_extend = new byte[4];
        byteBuf.readBytes(tmp_capability_and_extend);
        // 能力以及扩展
        int  capability_and_extend = ByteUtilBigLittle.bytes2IntLittle(tmp_capability_and_extend);

        //最大包大小 统一小端解析
        ByteBuf maxPacket = byteBuf.readBytes(4);

        // 编码集 用于解析下面4字节的 字符串
        byte charSet = byteBuf.readByte();

        // 23 填充值
        ByteBuf filler = byteBuf.readBytes(23);

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

        int pwdLen = byteBuf.readByte();
        byte[] passwordArray = new byte[pwdLen];
        byteBuf.readBytes(passwordArray);

        byte [] tmp = SecurityUtil.scramble411("root".getBytes(),ctx.channel().attr(PASSWORDPART).get());
        log.info("tmp == {}", Arrays.toString(tmp));
        log.info("pwd == {}", Arrays.toString(passwordArray));
        return true;
    }

}
