package com.haojiangbo.constant;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;


/**
  *
  * 反向代理key
 　　* @author 郝江波
 　　* @date 2020/4/15 14:23
 　　*/
public class NettyProxyMappingConstant {
     /**
      * 服务器端 反向代理的  key
      */
    public static final AttributeKey<Channel> MAPPING = AttributeKey.newInstance("PROXY_MAPPING");
    /**
     * 服务器端 会话
     */
    public static final AttributeKey<String> SESSION = AttributeKey.newInstance("SESSION");







}
