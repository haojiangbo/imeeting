package com.haojiangbo.protocol.proto;

import com.alibaba.druid.util.MySqlUtils;
import com.haojiangbo.utils.MySqlProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ResponsePackert {

    /**
     * 返回成功消息
     * @param ctx
     */
    public void success(final ChannelHandlerContext ctx) {
        // 此处的固定值 参考mysql 官网
        // https://dev.mysql.com/doc/internals/en/packet-OK_Packet.html
        ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(AbstratorMySqlPacket.AUTH_OK);
        ctx.writeAndFlush(byteBuf);
    }
    public void success(final ChannelHandlerContext ctx,byte packetId) {
        byte[] b =  AbstratorMySqlPacket.AUTH_OK.clone();
        b[3] = packetId;
        ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(b);
        ctx.writeAndFlush(byteBuf);
    }

    /**
     * 字符串转 LengthEncodedString
     * @param str
     * @return
     */
    public static byte[] readLengthEncodedString(byte[] str){
        int  len = str.length;
        byte[] lenArray =  MySqlProtocolUtils.intToLenencInt(len);
        byte [] r = new byte[len + lenArray.length];
        // 拷贝字符串内容
        System.arraycopy(str,0,r,lenArray.length,len);
        //拷贝长度内容
        System.arraycopy(lenArray,0,r,0,lenArray.length);
        return r;
    }

}
