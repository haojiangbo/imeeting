package com.haojiangbo.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 事件通知
 * 　　* @author 郝江波
 * 　　* @date 2020/4/18 10:34
 */
@Data
@Accessors(chain = true)
public class EventMessage {
    /**
     * 重新加载事件
     */
    public static final int RELOAD_EVENT = 0XFF;
    /**
     * 关闭事件
     */
    public static final int CLOSE_EVENT = 0XEE;
    private int type;
    private String message;
}
