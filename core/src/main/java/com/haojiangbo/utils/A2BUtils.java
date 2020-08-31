package com.haojiangbo.utils;


import com.haojiangbo.constant.NettyProxyMappingConstant;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * 关系映射工具类
 *
 * 　　* @author 郝江波
 * 　　* @date 2020/4/16 17:04
 *
 */
public class A2BUtils {


    /**
     * 添加关系
     * @param src
     * @param dest
     */
    public static void addMapping(Channel src, Channel dest) {
      /*  src.attr(NettyProxyMappingConstant.MAPPING).set(dest);
        dest.attr(NettyProxyMappingConstant.MAPPING).set(src);*/
        addMapping(src ,dest,NettyProxyMappingConstant.MAPPING);
    }


    /**
     * 添加关系
     * @param src
     * @param dest
     */
    public static void addMapping(Channel src, Channel dest, AttributeKey attributeKey) {
        src.attr(attributeKey).set(dest);
        dest.attr(attributeKey).set(src);
    }



    /**
     * 移除关系
     * @param src
     */
    public static void removeMapping(Channel src) {
        removeMapping(src,NettyProxyMappingConstant.MAPPING);
    }
    /**
     * 移除关系
     * @param src
     */
    public static void removeMapping(Channel src,AttributeKey<Channel>  attributeKey) {
        Channel target = src.attr(attributeKey).get();
        if (null != target) {
            target.attr(attributeKey).set(null);
        }
        src.attr(attributeKey).set(null);
    }




}
