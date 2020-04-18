package com.haojiangbo.hander;

import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
  *
  * 哨兵客户端
 　　* @author 郝江波
 　　* @date 2020/4/16 17:40
 　　*/
 @Slf4j
public class SentryClientHander extends ChannelInboundHandlerAdapter {


     @Override
     public void channelRead(ChannelHandlerContext ctx, Object msg) {
        CustomProtocol customProtocol = (CustomProtocol) msg;
        Channel target =  ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        if(null != target && target.isActive()){
            target.writeAndFlush(customProtocol.getContent());
        }else{
            ctx.close();
            ReferenceCountUtil.release(customProtocol);
        }
     }


     @Override
     public void channelInactive(ChannelHandlerContext ctx) throws Exception {
         Channel target =  ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
         if(null != target){
             target.close();
         }
         log.error("无法访问对端连接  强制关闭当前连接");
         ctx.close();
     }
 }
