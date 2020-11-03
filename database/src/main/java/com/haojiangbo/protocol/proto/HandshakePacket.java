package com.haojiangbo.protocol.proto;

import com.haojiangbo.protocol.config.Capabilities;
import com.haojiangbo.utils.ByteUtilBigLittle;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *


 1              [0a] protocol version
 string[NUL]    server version
 4              connection id
 string[8]      auth-plugin-data-part-1
 1              [00] filler
 2              capability flags (lower 2 bytes)
 if more data in the packet:
 1              character set
 2              status flags
 2              capability flags (upper 2 bytes)
 if capabilities & CLIENT_PLUGIN_AUTH {
 1              length of auth-plugin-data
 } else {
 1              [00]
 }
 string[10]     reserved (all [00])
 if capabilities & CLIENT_SECURE_CONNECTION {
 string[$len]   auth-plugin-data-part-2 ($len=MAX(13, length of auth-plugin-data - 8))
 if capabilities & CLIENT_PLUGIN_AUTH {
 string[NUL]    auth-plugin name
 }

 Fields

 protocol_version (1) -- 0x0a protocol_version

 server_version (string.NUL) -- human-readable server version

 connection_id (4) -- connection id

 auth_plugin_data_part_1 (string.fix_len) -- [len=8] first 8 bytes of the auth-plugin data

 filler_1 (1) -- 0x00

 capability_flag_1 (2) -- lower 2 bytes of the Protocol::CapabilityFlags (optional)

 character_set (1) -- default server character-set, only the lower 8-bits Protocol::CharacterSet (optional)

 This “character set” value is really a collation ID but implies the character set; see the Protocol::CharacterSet description.

 status_flags (2) -- Protocol::StatusFlags (optional)

 capability_flags_2 (2) -- upper 2 bytes of the Protocol::CapabilityFlags

 auth_plugin_data_len (1) -- length of the combined auth_plugin_data, if auth_plugin_data_len is > 0

 auth_plugin_name (string.NUL) -- name of the auth_method that the auth_plugin_data belongs to

 * 连接后服务器向客户端发送的握手协议
 */
@Data
@Slf4j
public class HandshakePacket  extends  AbstratorMySqlPacket{

    /**
     * 填充 10字节 参考 官网文档
     */
    public static final byte []
            filler = new byte[10];
    /**
     * 填充0
     */
    public static final byte fillerByte = 0;
    /**
     * 协议版本号
     * string[NUL]
     */
    public byte protoVersion = 0x0a;
    /**
     * mysql版本号
     */
    public String serverVersion;
    /**
     * 线程ID
     */
    public int threadId;
    /**
     * 挑战随机数1 8个字节
     * string[8]
     */
    public byte [] authPluginDataPart1;

    /**
     * 服务器全能位 低 16 位
     */
    public short capabilityflagsLittle ;
    /**
     * 编码集 utf-8mb4
     */
    public byte charset = (byte) 0xe0;
    /**
     *  服务器状态位
     */
    public byte[] statusFlags = new byte[]{(byte) 0x02, (byte) 0x00};
    /**
     * 服务器全能位 高 16 位
     */
    public short capabilityflagsUpper ;
    /**
     * 加密方式的字节长度
     */
    public byte authPluginDataLen = 0;
    /**
     * 挑战随机数2  12个 字节
     */
    public byte [] authPluginDataPart2;





    public void write(Channel channel){
        int calcSize = calcSize();
        // calcSize 不包含 数据包长度和 序列号  一共占用4个字节
        ByteBuf byteBuf = channel.alloc().buffer(calcSize+4);
        // 数据大小 以及 序列号 小端存储方式发送 java是大端存储 需要移位
        byteBuf.writeByte(calcSize & 0xff);
        byteBuf.writeByte(calcSize >>> 8);
        byteBuf.writeByte(calcSize >>> 16);
        byteBuf.writeByte(0);
        // 版本信息
        byteBuf.writeByte(this.protoVersion);
        byteBuf.writeBytes(this.serverVersion.getBytes());
        byteBuf.writeByte(fillerByte);
        // 此处是threadId
        byteBuf.writeInt(2 << 24);
        // 挑战随机数
        byteBuf.writeBytes(this.authPluginDataPart1);
        byteBuf.writeByte(fillerByte);
        //权能标记位
        int capabilities =   getServerCapabilities();
        //权能标记 高16位 对应 mysql 官网的 lower 2 bytes
        byteBuf.writeByte(capabilities);
        byteBuf.writeByte(capabilities >>> 8);
        // 编码集
        byteBuf.writeByte(this.charset);
        // 服务器状态
        byteBuf.writeBytes(this.statusFlags);
        //权能标记 低16位 对应 mysql 官网的 upper 2 bytes
        byteBuf.writeByte(capabilities >>> 16);
        byteBuf.writeByte(capabilities >>> 24);
        //长度 填充0
        byteBuf.writeByte(this.authPluginDataLen);
        // 填充10个字节
        byteBuf.writeBytes(filler);
        // 挑战随机数2 此处的末尾补0 官网没写 但是通过抓包
        // 发现是需要携带的
        byteBuf.writeBytes(this.authPluginDataPart2);
        byteBuf.writeByte(fillerByte);

        // 打印协议16内容
        StringBuilder stb = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(stb,byteBuf);
        log.info("\n服务端发起的握手协议\n"+stb.toString());
        // 发送
        channel.writeAndFlush(byteBuf);
    }

    private int calcSize(){
        // 3位数据长度 1位序列号
        int i = 0;
        // 协议版本
        i += 1;
        // 服务器version
        i += this.serverVersion.getBytes().length;
        i += 1;
        // threadId
        i += 4;
        // 挑战随机数1 authPluginDataPart1
        i += this.authPluginDataPart1.length;
        i += 1;
        // capabilityflagsLittle  服务器权能标记位 低16位
        i += 2;
        // charset 编码集
        i += 1;
        // statusFlags 服务器状态
        i += this.statusFlags.length;
        // capabilityflagsUpper 服务器权能标记位 高16位
        i += 2;
        // authPluginDataLen 1个字节
        i += 1;
        // 填充字节10位
        i += filler.length;
        // 挑战随机数2
        i += this.authPluginDataPart2.length;
        i += 1;

        return  i;
    }



    protected int getServerCapabilities() {
        int flag = 0;
        flag |= Capabilities.CLIENT_LONG_PASSWORD;
        flag |= Capabilities.CLIENT_FOUND_ROWS;
        flag |= Capabilities.CLIENT_LONG_FLAG;
        flag |= Capabilities.CLIENT_CONNECT_WITH_DB;
        // flag |= Capabilities.CLIENT_NO_SCHEMA;
        // flag |= Capabilities.CLIENT_COMPRESS;
        flag |= Capabilities.CLIENT_ODBC;
        // flag |= Capabilities.CLIENT_LOCAL_FILES;
        flag |= Capabilities.CLIENT_IGNORE_SPACE;
        flag |= Capabilities.CLIENT_PROTOCOL_41;
        flag |= Capabilities.CLIENT_INTERACTIVE;
        // flag |= Capabilities.CLIENT_SSL;
        flag |= Capabilities.CLIENT_IGNORE_SIGPIPE;
        flag |= Capabilities.CLIENT_TRANSACTIONS;
        // flag |= ServerDefs.CLIENT_RESERVED;
        flag |= Capabilities.CLIENT_SECURE_CONNECTION;
        return flag;
    }


}
