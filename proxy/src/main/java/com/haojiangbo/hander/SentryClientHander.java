package com.haojiangbo.hander;

import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
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
       /* if(customProtocol.getMeesgeType() == ConstantValue.CONCAT){
            target.config().setOption(ChannelOption.AUTO_READ,true);
            return;
        }*/
        boolean b = (null != target && target.isActive());
        log.info("RRR3 哨兵 clentHander 向用户发送数据 {} byte 结果 {}",customProtocol.getContent().readableBytes(),b);
        if(b){
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
         log.error("哨兵客户端-桥接-连接已关闭");
         ctx.close();
     }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
         super.exceptionCaught(ctx, cause);
    }
}
