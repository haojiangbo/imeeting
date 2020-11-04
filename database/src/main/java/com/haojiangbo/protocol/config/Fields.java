package com.haojiangbo.protocol.config;
/**
 * 字段类型及标识定义
 *
 * @author lizhuyang
 */
public class Fields {
    /** field data type */
   public static int FIELD_TYPE_DECIMAL = 0;
    public static int FIELD_TYPE_TINY = 1;
    public static int FIELD_TYPE_SHORT = 2;
    public static int FIELD_TYPE_LONG = 3;
    public static int FIELD_TYPE_FLOAT = 4;
    public static int FIELD_TYPE_DOUBLE = 5;
    public static int FIELD_TYPE_NULL = 6;
    public static int FIELD_TYPE_TIMESTAMP = 7;
    public static int FIELD_TYPE_LONGLONG = 8;
    public static int FIELD_TYPE_INT24 = 9;
    public static int FIELD_TYPE_DATE = 10;
    public static int FIELD_TYPE_TIME = 11;
    public static int FIELD_TYPE_DATETIME = 12;
    public static int FIELD_TYPE_YEAR = 13;
    public static int FIELD_TYPE_NEWDATE = 14;
    public static int FIELD_TYPE_VARCHAR = 15;
    public static int FIELD_TYPE_BIT = 16;
    public static int FIELD_TYPE_NEW_DECIMAL = 246;
    public static int FIELD_TYPE_ENUM = 247;
    public static int FIELD_TYPE_SET = 248;
    public static int FIELD_TYPE_TINY_BLOB = 249;
    public static int FIELD_TYPE_MEDIUM_BLOB = 250;
    public static int FIELD_TYPE_LONG_BLOB = 251;
    public static int FIELD_TYPE_BLOB = 252;
    public static int FIELD_TYPE_VAR_STRING = 253;
    public static int FIELD_TYPE_STRING = 254;
    public static int FIELD_TYPE_GEOMETRY = 255;

    /** field flag */
    public static  int NOT_NULL_FLAG = 0x0001;
    public static  int PRI_KEY_FLAG = 0x0002;
    public static  int UNIQUE_KEY_FLAG = 0x0004;
    public static  int MULTIPLE_KEY_FLAG = 0x0008;
    public static  int BLOB_FLAG = 0x0010;
    public static  int UNSIGNED_FLAG = 0x0020;
    public static  int ZEROFILL_FLAG = 0x0040;
    public static  int BINARY_FLAG = 0x0080;
    public static  int ENUM_FLAG = 0x0100;
    public static  int AUTO_INCREMENT_FLAG = 0x0200;
    public static  int TIMESTAMP_FLAG = 0x0400;
    public static  int SET_FLAG = 0x0800;
}
