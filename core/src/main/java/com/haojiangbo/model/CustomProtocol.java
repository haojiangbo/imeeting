package com.haojiangbo.model;

import com.haojiangbo.constant.ConstantValue;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.experimental.Accessors;
 /**
  *
  * 自定义协议
 　　* @author 郝江波
 　　* @date 2020/4/17 10:38
 　　*/
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
     */
    public CustomProtocol(int meesgeType,
                          int sessionIdLengtn,
                          String sessionId,
                          int contentLength,
                          ByteBuf content) {
        this.contentLength = contentLength;
        this.content = content;
        this.meesgeType = meesgeType;
        this.sesstionIdLength = sessionIdLengtn;
        this.sessionId = sessionId;
    }



}
