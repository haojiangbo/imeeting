package com.haojiangbo.hander;

import com.haojiangbo.container.EventClientEngineContainner;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.shell.CmdShellHander;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
 /**
 　　* @author 江波
 　　* @date 2020/4/18 13:54
 　　*/
public class EventClientHander extends ChannelInboundHandlerAdapter {
    public static Channel channel = null;

    private EventClientEngineContainner eventClientEngineContainner;

    public EventClientHander(EventClientEngineContainner eventClientEngineContainner){
        this.eventClientEngineContainner = eventClientEngineContainner;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
        CmdShellHander.println("已连接成功...");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            CustomProtocol customProtocol = (CustomProtocol) msg;
            CmdShellHander.println(customProtocol.getContent().toString(CharsetUtil.UTF_8));
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        CmdShellHander.println("连接已断开...");
        eventClientEngineContainner.restart();
        super.channelInactive(ctx);
    }
}
