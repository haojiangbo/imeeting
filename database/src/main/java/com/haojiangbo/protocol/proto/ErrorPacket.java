package com.haojiangbo.protocol.proto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;

/**
 * https://dev.mysql.com/doc/internals/en/packet-ERR_Packet.html
* @Title: ErrorPacket
* @Package com.haojiangbo.protocol.proto
* @Description: 错误code
* @author Administrator
* @date 2020/11/3
* @version V1.0
*/
public class ErrorPacket extends ResponsePackert{
    public   byte packetId = 1;

    /**
     * 错误包是固定值
     */
    public byte header = (byte) 0xff;
    public int  errcode;


    private static final byte SQLSTATE_MARKER = (byte) '#';
    private static final byte[] DEFAULT_SQLSTATE = "HY000".getBytes();


    public String message;

    public void write(Channel channel){
        int size =  calcSize();
        ByteBuf byteBuf  = channel.alloc().buffer(size + 4);
        byteBuf.writeByte(size);
        byteBuf.writeByte(size >>> 8);
        byteBuf.writeByte(size >>> 16);

        byteBuf.writeByte(packetId);

        byteBuf.writeByte(this.header);
        byteBuf.writeByte(this.errcode);
        byteBuf.writeByte(this.errcode >>> 8);

        byteBuf.writeByte(SQLSTATE_MARKER);
        byteBuf.writeBytes(DEFAULT_SQLSTATE);

        byteBuf.writeBytes(this.message.getBytes());


    }

    int  calcSize(){
        int i = 0;
        // header
        i += 1;
        // errcode
        i += 2;

        // private static final byte SQLSTATE_MARKER = (byte) '#';
        // private static final byte[] DEFAULT_SQLSTATE = "HY000".getBytes();
        i += 5;

        // message;
        if(StringUtils.isNotEmpty(this.message)){
            i += this.message.getBytes().length;
        }
        return i;
    }

}
