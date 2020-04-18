package com.haojiangbo.hander;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
 /**
  * 事件处理器
 　　* @author 郝江波
 　　* @date 2020/4/18 10:54
 　　*/
public class EventServerHander  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {

        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
