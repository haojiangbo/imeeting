package com.haojiangbo.hander;
import com.haojiangbo.http.AbstractHttpParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

 /**
    *代理解析类
 　　* @author 郝江波
 　　* @date 2020/4/15 14:12
 　　*/
@Slf4j
public class ProxyHander extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf message = (ByteBuf) msg;
        AbstractHttpParser.getDefaltHttpParser().decode(message);
        super.channelRead(ctx, msg);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
       /* log.info("channelId = {} 断开连接",ctx.channel().id().asShortText());*/
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
