package com.haojiangbo.hander;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.http.AbstractHttpParser;
import com.haojiangbo.http.HttpRequest;
import com.haojiangbo.utils.AtoBUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;


/**
    *代理解析类
 　　* @author 郝江波
 　　* @date 2020/4/15 14:12
 　　*/
@Slf4j
public class ProxyServerHander extends ChannelInboundHandlerAdapter {

    private static Bootstrap bootstrap = null;

    /**
     * 此处使用常量代理
     * 因为反向代理 肯定会和哨兵服务器部署在同一个服务器上
     * 所以直接写成本机地址
     */
    private static final String HOST = "127.0.0.1";

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

        if(ServerConfig.INSTAND.isProxyModel()){
            ByteBuf read = message.copy();
            String m = read.toString(CharsetUtil.UTF_8);
            log.info("接受数据 ..... {}",m);
            if(m.startsWith("CONNECT")){
                ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
                // 解析http协议  此处可能有bug
                HttpRequest request =  AbstractHttpParser.getDefaltHttpParser().decode(message);
                createConnect(ctx, message, request,false);
                ctx.writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes()));
                ReferenceCountUtil.release(read);
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        if(target == null){
            ctx.channel().config().setOption(ChannelOption.AUTO_READ, false);
            // 解析http协议  此处可能有bug
            HttpRequest request =  AbstractHttpParser.getDefaltHttpParser().decode(message);
            createConnect(ctx, message, request,true);
        }else{
            if(!target.isActive()){
                ctx.close();
                return;
            }
            target.writeAndFlush(msg);
        }
    }

    private void createConnect(ChannelHandlerContext ctx, ByteBuf message, HttpRequest request,boolean isSendMessgae) {
        String domain = request.getHost().substring(0, request.getHost().indexOf("."));
        log.info("host = {}  domain = {}",request.getHost(),domain);
        try {
            Map<String,Integer> domainProtMapping = ServerConfig.INSTAND.getDomainProtMapping();

            //如果处在非代理模式下 则执行正常逻辑
            if(!ServerConfig.INSTAND.isProxyModel()){
                request.setHost(HOST);
                request.setPort(domainProtMapping.get(domain));
            }
            bootstrap.connect(request.getHost(), request.getPort()).addListener((ChannelFutureListener) future -> {
            //bootstrap.connect(HOST, domainProtMapping.get(domain)).addListener((ChannelFutureListener) future -> {
                if(future.isSuccess()){
                    // 相互映射 双方互相可以得到对方
                    AtoBUtils.addMapping(ctx.channel(),future.channel());
                    if(isSendMessgae){
                        future.channel().writeAndFlush(message);
                    }
                }else{
                    ctx.close();
                }
                // 允许服务端继续接收消息
                ctx.channel().config().setOption(ChannelOption.AUTO_READ, true);
            });
        }catch (Exception e){
            e.printStackTrace();
            ctx.close();
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        AtoBUtils.removeMapping(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
