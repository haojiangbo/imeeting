package com.haojiangbo.utils;

import io.netty.buffer.ByteBuf;

/**
* @Title: MySqlProtocolUtils
* @Package com.haojiangbo.utils
* @Description: mysqlbyte工具类
* @author 郝江波
* @date 2020/11/6
* @version V1.0
*/
public class MySqlProtocolUtils  extends ByteUtilBigLittle{


    /**
     * 如果这个值 小于 251 用一个字节存储 当前字节存储了长度 等于251 是空值
     * 如果这个值 等于252 那么后面的2个字节 才是真真存储 长度
     * 如果这个值 等于253 后面的3个字节存储了长度
     * 如果这个值 等于254 后面的 8 个字节存储了长度
     * If the value is ≥ (224) and < (264) it is stored as fe + 8-byte integer.
     * @param buf
     */
    public static int readLenencInt(ByteBuf buf){
        int len = buf.readByte() & 0xff;
        int v = 0;
        switch (len){
            case 251:
                return 0;
            case 252:
                v |= buf.readByte() & 0xff;
                v |= (buf.readByte() & 0xff) << 8;
                return v;
            case 253:
                v |= buf.readByte() & 0xff;
                v |= (buf.readByte() & 0xff) << 8;
                v |= (buf.readByte() & 0xff) << 16;
                return v;
            // 数据总长度才3个字节 我深深怀疑 mysql
            // 这个值是否可以真真的遇到
            case 254:
                v |= buf.readByte() & 0xff;
                v |= (long) (buf.readByte() & 0xff) << 8;
                v |= (long) (buf.readByte() & 0xff) << 16;
                v |= (long) (buf.readByte() & 0xff) << 24;
                v |= (long) (buf.readByte() & 0xff) << 32;
                v |= (long) (buf.readByte() & 0xff) << 40;
                v |= (long) (buf.readByte() & 0xff) << 48;
                v |= (long) (buf.readByte() & 0xff) << 56;
                return v;
            default:
                return len;
        }
    }

    /**
     *
     * int 转成对应的字节
     *
     * If the value is < 251, it is stored as a 1-byte integer.
     *
     * If the value is ≥ 251 and < (2^16), it is stored as fc + 2-byte integer.
     *
     * If the value is ≥ (2^16) and < (2^24), it is stored as fd + 3-byte integer.
     *
     * If the value is ≥ (2^24) and < (2^64) it is stored as fe + 8-byte integer.
     * @param input
     * @return
     */
    public static byte [] intToLenencInt(int input){
       if(input > 0 && input < 251){
           return new byte[]{(byte) (input & 0xff)};
       }else if(input >= 251 && input < Math.pow(16,2)){
            byte [] bytes = new byte[3];
            bytes[0] = (byte) 0xfc;
            bytes[1] = (byte) (input & 0xff);
            bytes[2] = (byte) ((input & 0xff) >>> 8);
       }else if(input >= Math.pow(16,2) && input < Math.pow(24,2)){
           byte [] bytes = new byte[4];
           bytes[0] = (byte) 0xfd;
           bytes[1] = (byte) (input & 0xff);
           bytes[2] = (byte) ((input & 0xff) >>> 8);
           bytes[3] = (byte) ((input & 0xff) >>> 16);
       }else if(input >= Math.pow(24,2) && input < Math.pow(64,2)){
           throw  new RuntimeException("不支持的数据大小");
       }else{
           return  new byte[]{(byte) 0xfb};
       }
       return null;
    }



}
