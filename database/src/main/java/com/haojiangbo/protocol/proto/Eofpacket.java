package com.haojiangbo.protocol.proto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
* @Title: Eofpacket
* @Package com.haojiangbo.protocol.proto
* @Description: EOF包
* @author 郝江波
* @date 2020/11/4
* @version V1.0
*/
public class Eofpacket extends ResponsePackert {

    public byte packetId;

    /**
     * 固定包头
     */
    public byte header = (byte) 0xfe;
    /**
     * 0个警告
     */
    public byte[] warning = new byte[]{0x00,0x00};
    /**
     * 固定状态
     */
    public byte[] status_flags = new byte[]{0x02,0x00};


    public void write(Channel channel){
        int calcSize = calcSize();
        ByteBuf byteBuf = channel.alloc().buffer(calcSize + 4);
        byteBuf.writeByte(calcSize);
        byteBuf.writeByte(calcSize >>> 8);
        byteBuf.writeByte(calcSize >>> 16);
        byteBuf.writeByte(this.packetId);
        byteBuf.writeByte(this.header);
        byteBuf.writeBytes(this.warning);
        byteBuf.writeBytes(this.status_flags);
        channel.writeAndFlush(byteBuf);
    }

    int calcSize(){
        int i = 0;
        i += 1;
        i += warning.length;
        i += status_flags.length;
        return i;
    }


}
