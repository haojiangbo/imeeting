package com.haojiangbo.hander;
import com.haojiangbo.http.AbstractHttpParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyHander extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf message = (ByteBuf) msg;
      /*  log.info("capacity 的 大小 {}",message.capacity());
        String str  =  message.toString(CharsetUtil.UTF_8);
        log.info("channelId = {}",ctx.channel().id().asLongText());
        log.info("\r\n"+str);*/
        AbstractHttpParser.getDefaltHttpParser().decode(message);
        ctx.writeAndFlush(message);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channelId = {} 断开连接",ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
