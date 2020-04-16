package com.haojiangbo.config;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
 /**
  *
  *
  * 会话的映射
 　　* @author 郝江波
 　　* @date 2020/4/16 9:47
 　　*/
public class SessionChannelMapping {
    /**
     * 会话映射
     */
    public static final ConcurrentHashMap<String, Channel> SESSION_CHANNEL_MAPPING = new ConcurrentHashMap();
}
