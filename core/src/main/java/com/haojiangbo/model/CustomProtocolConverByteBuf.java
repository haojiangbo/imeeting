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
        // 消息头
        out.writeInt(msg.getHeadData());
        out.writeInt(msg.getMeesgeType());
        out.writeInt(msg.getClientId());

        // 会话
        out.writeInt(msg.getSesstionIdLength());
        out.writeBytes(msg.getSessionId().getBytes());

        // 负载
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
        return out;
    }
}
