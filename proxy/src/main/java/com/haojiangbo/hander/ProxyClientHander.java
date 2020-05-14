package com.haojiangbo.hander;

import com.haojiangbo.constant.NettyProxyMappingConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
  *
  *  客户端数据交换
 　　* @author hjb
 　　* @date 2020/4/15 15:25
 　　*/
 @Slf4j
public class ProxyClientHander extends ChannelInboundHandlerAdapter {

     @Override
     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
         log.info("反向代理引擎交换数据 {} byte", ((ByteBuf)msg).readableBytes());
         Channel target =  ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
         if(null == target || !target.isActive()){
             ctx.close();
         }else{
             target.writeAndFlush(msg);
         }
     }
 }
