package com.haojiangbo.model;

import com.haojiangbo.constant.ConstantValue;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Arrays;

@Data
@Accessors(chain = true)
public class CustomProtocol {
    /**
     * 消息的开头的信息标志
     */
    private int headData = ConstantValue.HEAD_DATA;
    /**
     * 消息类型
     */
    private int meesgeType;
    /**
     * 客户端ID
     */
    private int clientId;
    /**
     * 会话ID的长度
     */
    private int sesstionIdLength;
    /**
     * 会话ID
     */
    private String sessionId;
    /**
     * 消息的长度
     */
    private int contentLength;
    /**
     * 消息的内容
     */
    private ByteBuf content;
    /**
     * 初始化协议
     * @param meesgeType 消息类型
     * @param clientId 客户端ID
     * @param contentLength 消息长度
     * @param content 消息内容
     */
    public CustomProtocol(int meesgeType,
                          int clientId,
                          int sessionIdLengtn,
                          String sessionId,
                          int contentLength,
                          ByteBuf content) {
        this.contentLength = contentLength;
        this.content = content;
        this.meesgeType = meesgeType;
        this.clientId = clientId;
        this.sesstionIdLength = sessionIdLengtn;
        this.sessionId = sessionId;
    }



}
