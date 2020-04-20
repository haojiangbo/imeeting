package com.haojiangbo.constant;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;


/**
  *
  *  常量配置
 　　* @author 郝江波
 　　* @date 2020/4/15 14:23
 　　*/
public class NettyProxyMappingConstant {
     /**
      *  映射
      */
    public static final AttributeKey<Channel> MAPPING = AttributeKey.newInstance("PROXY_MAPPING");
    /**
     *  会话
     */
    public static final AttributeKey<String> SESSION = AttributeKey.newInstance("SESSION");







}
