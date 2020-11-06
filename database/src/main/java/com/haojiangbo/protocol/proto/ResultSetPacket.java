package com.haojiangbo.protocol.proto;

import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.datamodel.HDatabaseTableModel;
import com.haojiangbo.protocol.config.Fields;
import com.haojiangbo.utils.ByteUtilBigLittle;
import com.haojiangbo.utils.MetaDataUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition
 *
 * <p>
 *     这个类型的packet由4部分组成
 *  第一部分 列数量
 *     packet [长度[3],序列[1],和[1-9]字节的列数]
 *  第二部分 列定义
 *     packet [
 *      长度3
 *      序列2
 *      lenenc str 就是一个字节 代表字符串长度 后面跟着字符串
        catalog (lenenc_str) -- catalog (always "def")
        schema (lenenc_str) -- schema-name
        table (lenenc_str) -- virtual table-name
        org_table (lenenc_str) -- physical table-name
        name (lenenc_str) -- virtual column name
        org_name (lenenc_str) -- physical column name
        next_length (lenenc_int) -- length of the following fields (always 0x0c)
        character_set (2) -- is the column character
        column_length (4) -- maximum length of the field
        column_type (1) -- type of the column as defined in Column Type
        flags (2) -- flags
        decimals (1) -- max shown decimal digits
                                   -  0x00 for integers and static strings

                                   -  0x1f for dynamic strings, double, float

                                   -  0x00 to 0x51 for decimals
         2   filler [00] [00] 填充值

 *     ]

    第三部分 EOF包


 * </p>
 *
 *
 *
* @Title: ResultSetPacket
* @Package com.haojiangbo.protocol.proto
* @Description: ResultSetPacket
* @author 郝江波
* @date 2020/11/4
* @version V1.0
*/
public class ResultSetPacket extends  ResponsePackert{
    public byte packetId;
    // 列数量
    public byte column;


    private List<ColumnDefinition41> columns  = new LinkedList<>();;
    private List<ResultsetRow> rows = new LinkedList<>();;

    public ResultSetPacket buildHeader(JSONObject metaData,String databaseName,String tableName){
        this.column = (byte) MetaDataUtils.calcMetaDataSize(metaData);
        return buildColumnDefinition41(metaData,databaseName,tableName);
    }

    public ResultSetPacket buildColumnDefinition41(JSONObject metaData,String databaseName,String tableName){
        Set<String> keys =  metaData.keySet();
        for(String key : keys){
            ColumnDefinition41 cdf = new ColumnDefinition41();
            cdf.schema = databaseName;
            cdf.table = tableName;
            cdf.orgTable = tableName;
            cdf.name = key;
            cdf.orgName = key;
            // 暂时不考虑类型
           /* byte b =  metaData.getByte(key).byteValue();
            if(b == ColumnVauleType.BIGINT.getValue()){
                cdf.columnType = Fields.FIELD_TYPE_LONG
            }else{

            }*/
            this.columns.add(cdf);
        }
        return this;
    }

    public ResultSetPacket buildRowData(List<String[]> rowData){
        for(String[] row : rowData){
            ResultsetRow resultsetRow = new ResultsetRow();
            resultsetRow.values = Arrays.asList(row);
            this.rows.add(resultsetRow);
        }
        return this;
    }


    public void write(BaseMysqlPacket baseMysqlPacket){
        Channel channel = baseMysqlPacket.context.channel();
        baseMysqlPacket.packetNumber ++;
        this.packetId =  baseMysqlPacket.packetNumber;
        int calcSize = 1;
        // 头部分
        ByteBuf buf =  channel.alloc().buffer(calcSize + 4);
        buf.writeByte(calcSize);
        buf.writeByte(calcSize >>> 8);
        buf.writeByte(calcSize >>> 16);
        buf.writeByte(this.packetId);
        buf.writeByte(this.column);
        channel.writeAndFlush(buf);

        for(ColumnDefinition41 cdf : this.columns){
            baseMysqlPacket.packetNumber ++;
            cdf.packetId  = baseMysqlPacket.packetNumber;
            cdf.write(channel);
        }
        //EOF包
        baseMysqlPacket.packetNumber ++;
        Eofpacket eofpacket = new Eofpacket();
        eofpacket.packetId =  baseMysqlPacket.packetNumber;
        eofpacket.write(channel);

        //row数据
        for(ResultsetRow row : this.rows){
            baseMysqlPacket.packetNumber ++;
            row.packetId  = baseMysqlPacket.packetNumber;
            row.write(channel);
        }

        //EOF包
        baseMysqlPacket.packetNumber ++;
        eofpacket = new Eofpacket();
        eofpacket.packetId =  baseMysqlPacket.packetNumber;
        eofpacket.write(channel);

    }

    public void write(Channel channel, BaseMysqlPacket baseMysqlPacket){
        baseMysqlPacket.packetNumber ++;

        this.packetId =  baseMysqlPacket.packetNumber;
        this.column = 1;
        int calcSize = 1;
        // 头部分
        ByteBuf buf =  channel.alloc().buffer(calcSize + 4);
        buf.writeByte(calcSize);
        buf.writeByte(calcSize >>> 8);
        buf.writeByte(calcSize >>> 16);
        buf.writeByte(this.packetId);
        buf.writeByte(this.column);
        channel.writeAndFlush(buf);

        //列定义
        baseMysqlPacket.packetNumber ++;
        ColumnDefinition41 cdf = new ColumnDefinition41();
        cdf.packetId  = baseMysqlPacket.packetNumber;
        cdf.schema = "information_schema";
        cdf.table = "config";
        cdf.orgTable = "config";
        cdf.name = "database";
        cdf.orgName = "database";
        cdf.write(channel);

        //EOF包
        baseMysqlPacket.packetNumber ++;
        Eofpacket eofpacket = new Eofpacket();
        eofpacket.packetId =  baseMysqlPacket.packetNumber;
        eofpacket.write(channel);

        //row数据
        baseMysqlPacket.packetNumber ++;
        ResultsetRow resultsetRow = new ResultsetRow();
        resultsetRow.packetId = baseMysqlPacket.packetNumber;
        resultsetRow.values = new LinkedList<>();
        resultsetRow.values.add(HDatabaseTableModel.DEFULT_DATABASE_NAME);
        resultsetRow.write(channel);

        //EOF包
        baseMysqlPacket.packetNumber ++;
        eofpacket = new Eofpacket();
        eofpacket.packetId =  baseMysqlPacket.packetNumber;
        eofpacket.write(channel);

    }




    /**
     * 列定义
     */
    // lenenc_str
    public  static class ColumnDefinition41{

        public byte packetId ;

        public String catalog = "def";
        // 库名
        public String schema;
        // 虚拟表名
        public String table;
        // 物理表名
        public String orgTable;
        // 虚拟列名
        public String name;
        // 物理列名
        public String orgName;
        // 固定0x0c
        public byte nextLength = 0x0c;
        // 编码集
        public byte[] charSet = new byte[]{0x21,0x00};
        // 列长度
        public int columnLength = 256 ;
        // 列类型
        public byte columnType = (byte) (Fields.FIELD_TYPE_VAR_STRING & 0xff);
        // 标记位
        public byte [] flags = new byte[]{0x01,0x00};
        // 浮点数标记位
        public byte  decimals = 0x00;
        // 填充
        public byte[] filler = new byte[]{0x00,0x00};

        public void write(Channel channel){
            int calcSize = calcSize();
            ByteBuf buf =  channel.alloc().buffer(calcSize + 4);
            buf.writeByte(calcSize);
            buf.writeByte(calcSize >>> 8);
            buf.writeByte(calcSize >>> 16);
            buf.writeByte(this.packetId);
            buf.writeBytes(ResponsePackert.readLengthEncodedString(this.catalog));
            buf.writeBytes(ResponsePackert.readLengthEncodedString(this.schema));
            buf.writeBytes(ResponsePackert.readLengthEncodedString(this.table));
            buf.writeBytes(ResponsePackert.readLengthEncodedString(this.orgTable));
            buf.writeBytes(ResponsePackert.readLengthEncodedString(this.name));
            buf.writeBytes(ResponsePackert.readLengthEncodedString(this.orgName));
            buf.writeByte(this.nextLength);
            buf.writeBytes(this.charSet);
            buf.writeBytes(ByteUtilBigLittle.intToByteLittle(this.columnLength));
            buf.writeByte(this.columnType);
            buf.writeBytes(this.flags);
            buf.writeByte(this.decimals);
            buf.writeBytes(this.filler);
            channel.writeAndFlush(buf);
        }

        private int calcSize(){
            int i = 0;
            i += this.catalog.getBytes().length + 1;
            i += this.schema.getBytes().length + 1;
            i += this.table.getBytes().length + 1;
            i += this.orgTable.getBytes().length + 1;
            i += this.name.getBytes().length + 1;
            i += this.orgName.getBytes().length + 1;
            i += 1;
            i += this.charSet.length;
            i += 4;
            i += 1;
            i += this.flags.length;
            i += 1;
            i += this.filler.length;
            return i;
        }

    }

    // 行格式

    /**
     * 每一行的数据
     *
     * NULL  使用 0xfb 表示
     * 其他的全部都是字符串 以 strlenenc表示 即 1字节长度 字符串内容
     * everything else is converted into a string and is sent as Protocol::LengthEncodedString.
     */
    public static class ResultsetRow{
        public byte packetId;
        public List<String> values;
        public void write(Channel channel){
            int len = 0;
            for(String value : this.values){
                byte [] v =  ResponsePackert.readLengthEncodedString(value);
                len += v.length;
            }
            ByteBuf buf =  channel.alloc().buffer(len + 4);
            buf.writeByte(len);
            buf.writeByte(len >>> 8);
            buf.writeByte(len >>> 16);
            buf.writeByte(this.packetId);
            for(String value : this.values){
                byte [] v =  ResponsePackert.readLengthEncodedString(value);
                buf.writeBytes(v);
            }
            channel.writeAndFlush(buf);
        }
    }

}
