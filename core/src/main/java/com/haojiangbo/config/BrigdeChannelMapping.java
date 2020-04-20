package com.haojiangbo.config;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * 客户端向服务端发起连接
 *
 * 因为客户端携带了 clientid
 *
 * 需要做一个映射 哪个clientId
 *
 * 代表哪个管道
 *
 *
  * @author 郝江波
  * @date 2020/4/15 17:19
  */
public class BrigdeChannelMapping {
   /**
    * clientId 的 映射
    */
   public static ConcurrentHashMap<String, Channel> CLIENT_ID_MAPPING = new ConcurrentHashMap<>();
    /**
     * 管道ID 和  clientId 的映射
     */
   public static ConcurrentHashMap<String, String> CHANNELID_CLINENTID_MAPPING = new ConcurrentHashMap<>();







}
