package com.haojiangbo.protocol;

import lombok.Data;

/**
 * 该协议使用TCP传输
 * 用于控制，呼叫，接听，挂断等操作
 *
 * @author 郝江波
 * @version V1.0
 * @Title: ControlProtocol
 * @Package com.haojiangbo.protocol
 * @Description: 控制协议
 * @date 2020/11/19
 */
@Data
public class ControlProtocol {
    // 协议头
    private byte header;
    // 控制位
    private byte flag;
    // 会话长度
    private byte sessionSize;
    // 会话
    private byte[] session;
    // 数据长度
    private int dataSize;
    // 数据
    private byte[] data;
}
