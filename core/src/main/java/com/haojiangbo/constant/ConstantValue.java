package com.haojiangbo.constant;

/**
 * @author 郝江波
 * @version V1.0
 * @Title: ConstantValue
 * @Package com.haojiangbo.constant
 * @Description: 协议常量
 * @date 2020/4/15
 */
public class ConstantValue {
    /**
     * 固定头
     */
    public static final int HEAD_DATA = 0X76;
    /**
     * 心跳消息
     */
    public static final int PING = 0X77;
    /**
     * 数据消息
     */
    public static final int DATA = 0X78;
    /**
     * 转发消息
     */
    public static final int FORWARD = 0X79;
    /**
     * clientId错误  为什么用英文，因为这样会减少一些不必要的转码错误
     */
    public static final String CLIENTID_ERROR = "clientId is not exist";
    /**
     * 重复连接的错误
     */
    public static final String REPEATED_ERROR = "Repeated connection error  please close other connection";
    /**
     * 指令
     */
    public static final String CLI = "cli";
}
