package com.haojiangbo.mapping;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
  * <p>
  *
  *     电话号和channel的映射
  *
  *
  * </p>
  *
  *
 　　* @author 郝江波
 　　* @date 2020/11/20 16:24
 　　*/
public class CallNumberAndChannelMapping {
    /**
     * 电话号和tcpCHannel的映射
     */
    public static ConcurrentHashMap<String, Channel> NUMBER_CHANNEL_MAPPING = new ConcurrentHashMap<>(1024);
    /**
     * 电话号和udpCHannel的映射
     */
    public static ConcurrentHashMap<String, InetSocketAddress> UDP_NUMBER_CHANNEL_MAPPING = new ConcurrentHashMap<>(1024);
}
