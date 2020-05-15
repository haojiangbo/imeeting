package com.haojiangbo.utils;


import com.haojiangbo.constant.NettyProxyMappingConstant;
import io.netty.channel.Channel;

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
     *
     * @param src
     * @param dest
     */
    public static void addMapping(Channel src, Channel dest) {
        src.attr(NettyProxyMappingConstant.MAPPING).set(dest);
        dest.attr(NettyProxyMappingConstant.MAPPING).set(src);
    }

    /**
     * 移除关系
     * @param src
     */
    public static void removeMapping(Channel src) {
        Channel target = src.attr(NettyProxyMappingConstant.MAPPING).get();
        if (null != target) {
            target.attr(NettyProxyMappingConstant.MAPPING).set(null);
        }
        src.attr(NettyProxyMappingConstant.MAPPING).set(null);
    }

}
