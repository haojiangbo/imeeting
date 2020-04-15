package com.haojiangbo.model;

import com.haojiangbo.codec.CustomProtocolDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
/**
* @Title: CustomProtocolConverByteBuf
* @Package com.haojiangbo.model
* @Description: 自定义协议转换为byteBuf
* @author Administrator
* @date 2020/4/15
* @version V1.0
*/
public class CustomProtocolConverByteBuf {

    public static ByteBuf getByteBuf(CustomProtocol msg) {
        ByteBuf out = Unpooled.directBuffer(CustomProtocolDecoder.BASE_LENGTH+msg.getContent().length);
        return getByteBuf(out,msg);
    }
    public static ByteBuf getByteBuf(ByteBuf out,CustomProtocol msg) {
        // 1.写入消息的开头的信息标志(int类型)
        out.writeInt(msg.getHeadData());
        // 2.写入消息类型
        out.writeInt(msg.getMeesgeType());
        out.writeInt(msg.getClientId());
        // 3.写入消息的长度(int 类型)
        out.writeInt(msg.getContentLength());
        // 4.写入消息的内容(byte[]类型)
        out.writeBytes(msg.getContent());
        return out;
    }
}
