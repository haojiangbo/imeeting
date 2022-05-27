package com.haojiangbo.net.protocol;

import com.haojiangbo.ndkdemo.MainActivity;
import com.haojiangbo.net.tcp.ControlProtocolManager;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 该协议使用TCP传输
 * 用于控制，呼叫，接听，挂断等操作
 * 讨厌的get set方法 为了兼容安卓 不要了
 * @author 郝江波
 * @version V1.0
 * @Title: ControlProtocol
 * @Package com.haojiangbo.protocol
 * @Description: 控制协议
 * @date 2020/11/19
 */

public class ControlProtocol implements Serializable {
    //////////////// 控制位标记 ////////////////////////
    // 创建房间
    public static final byte CREATE = 1;
    // 创建房间回复
    public static final byte CREATE_REPLAY = 2;
    // 创建房间
    public static final byte JOIN = 3;
    // 创建房间回复
    public static final byte JOIN_REPLAY = 4;
    // 关闭房间
    public static final byte CLOSE = 5;
    // 关闭房间回复
    public static final byte CLOSE_REPLAY = 6;
    // 心跳消息
    public static final byte PING = 7;



    public static final byte HEADER = 0XF;
    // 协议头
    public byte header = HEADER;
    // 控制位
    public byte flag;
    // 会话长度
    public byte sessionSize;
    // 会话
    public byte[] session;
    // 数据长度
    public int dataSize;
    // 数据
    public byte[] data;

    public ControlProtocol(){};
    public ControlProtocol(byte flag, byte sessionSize, byte[] session, int dataSize, byte[] data) {
        this.flag = flag;
        this.sessionSize = sessionSize;
        this.session = session;
        this.dataSize = dataSize;
        this.data = data;
    }


    public ControlProtocol(byte flag, byte[] data) {
        this.flag = flag;
        this.session = ControlProtocolManager.getSessionId().getBytes();
        this.sessionSize = (byte) this.session.length;
        this.dataSize = data.length;
        this.data = data;
    }

}
