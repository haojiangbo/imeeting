package com.haojiangbo.config;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
  *
  *
  *     哨兵管道映射
 　　* @author 郝江波
 　　* @date 2020/4/15 17:34
 　　*/
public class SentryChannelMapping {
    /**
     * 端口和管道的映射
     */
    public static ConcurrentHashMap<Integer, Channel> PROT_CHANNEL_MAPPING = new ConcurrentHashMap<>();
    /**
     * 端口和clientId的映射
     */
    public static ConcurrentHashMap<Integer, Integer> PROT_ClIENT_ID_MAPPING = new ConcurrentHashMap<>();
    /**
     * 二级域名和CLIENTID的映射
     */
    public static ConcurrentHashMap<String, Integer> HOST_ClIENT_ID_MAPPING = new ConcurrentHashMap<>();
    /**
     * 二级域名和Channel的映射
     */
    public static ConcurrentHashMap<String, Channel> HOST_CHANNEL_MAPPING = new ConcurrentHashMap<>();


}
