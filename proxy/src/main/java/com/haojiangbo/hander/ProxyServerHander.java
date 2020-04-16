package com.haojiangbo.hander;
import com.haojiangbo.config.BrigdeChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.http.AbstractHttpParser;
import com.haojiangbo.http.HttpRequest;
import com.haojiangbo.model.CustomProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;


/**
    *代理解析类
 　　* @author 郝江波
 　　* @date 2020/4/15 14:12
 　　*/
@Slf4j
public class ProxyServerHander extends ChannelInboundHandlerAdapter {

    private static Bootstrap bootstrap = null;

    public ProxyServerHander(Bootstrap bsp){
        bootstrap = bsp;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf message = (ByteBuf) msg;
        Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        if(target == null){
            ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
            // 解析http协议  此处可能有bug
            HttpRequest request =  AbstractHttpParser.getDefaltHttpParser().decode(message);
            log.info("请求的域名>>>> {}", request.getHost());
            createConnect(ctx, message, request,80);
        }else{
            if(!target.isActive()){
                ctx.close();
                return;
            }
            target.writeAndFlush(msg);
        }
    }

    private void createConnect(ChannelHandlerContext ctx, ByteBuf message, HttpRequest request,int port) {
        bootstrap.connect("hdyq.hbweiyinqing.cn",port).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                // 相互映射 双方互相可以得到对方
                future.channel().attr(NettyProxyMappingConstant.MAPPING).set(ctx.channel());
                ctx.channel().attr(NettyProxyMappingConstant.MAPPING).set(future.channel());
                future.channel().writeAndFlush(message);
            }else{
                ctx.close();
            }
            // 允许服务端继续接收消息
            ctx.channel().config().setOption(ChannelOption.AUTO_READ, true);
        });
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        /*log.info("channelId = {} 断开连接",ctx.channel().id().asShortText());*/
        Channel target =  ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        if(null != target){
            target.attr(NettyProxyMappingConstant.MAPPING).set(null);
        }
        ctx.channel().attr(NettyProxyMappingConstant.MAPPING).set(null);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
