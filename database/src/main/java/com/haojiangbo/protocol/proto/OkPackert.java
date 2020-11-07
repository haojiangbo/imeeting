package com.haojiangbo.protocol.proto;

import com.haojiangbo.utils.MySqlProtocolUtils;
import io.netty.buffer.ByteBuf;

/**
* @Title: OkPackert
* @Package com.haojiangbo.protocol.proto
* @Description: (用一句话描述)
* @author Administrator
* @date 2020/11/3
* @version V1.0
*/
public class OkPackert extends ResponsePackert {

    public byte packetId;
    /**
     * header 固定头
     */
    public byte header = 0x00;
    /**
     * 影响行数
     */
    public byte [] affected_rows ;
    /**
     * 最后一个插入的ID
     */
    public byte [] last_insert_id;
    /**
     * 服务器状态
     */
    public  byte [] status_flags = new byte[]{0x02,0x00};
    /**
     * 警告数量
     */
    public  byte [] warnings = new byte[]{0x00,0x00};



    public void write(BaseMysqlPacket packet,long affectedRows, long lastRowId){
        this.affected_rows = MySqlProtocolUtils.intToLenencInt(affectedRows);
        this.last_insert_id = MySqlProtocolUtils.intToLenencInt(lastRowId);

        packet.packetNumber ++;
        this.packetId = packet.packetNumber;
        int size  = 0;
        size  += 1;
        size  += this.affected_rows.length;
        size  += this.last_insert_id.length;
        size  += this.status_flags.length;
        size  += this.warnings.length;

        ByteBuf buf = packet.context.alloc().buffer(size + 4);
        buf.writeByte(size & 0xff);
        buf.writeByte(size >>> 8);
        buf.writeByte(size >>> 16);
        buf.writeByte(this.packetId);
        buf.writeByte(this.header);
        buf.writeBytes(this.affected_rows);
        buf.writeBytes(this.last_insert_id);
        buf.writeBytes(this.status_flags);
        buf.writeBytes(this.warnings);
        packet.context.writeAndFlush(buf);

    }
}
