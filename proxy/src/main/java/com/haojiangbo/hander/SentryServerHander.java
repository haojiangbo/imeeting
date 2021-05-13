package com.haojiangbo.hander;

import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.model.CustomProtocolConverByteBuf;
import com.haojiangbo.utils.A2BUtils;
import com.haojiangbo.utils.SessionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 哨兵数据处理器
 　　* @author 郝江波
 　　* @date 2020/4/16 13:42
 　　*/
@Slf4j
public class SentryServerHander extends ChannelInboundHandlerAdapter {
    private String clientId = null;
    private Bootstrap clientBootstrap = null;
    private String clientUrl = null;
    public SentryServerHander(String clientId,
                              Bootstrap bootstrap,
                              String clientUrl
    ){
        this.clientBootstrap =bootstrap;
        this.clientId = clientId;
        this.clientUrl = clientUrl;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String sessionId = SessionUtils.genSessionId(this.clientId, this.clientUrl);
        //绑定sessionId
        ctx.channel().attr(NettyProxyMappingConstant.SESSION)
                .set(sessionId);
        SessionUtils.SessionModel model =  SessionUtils
                .parserSessionId(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());

        createConnect(ctx,Unpooled.wrappedBuffer("CONCAT".getBytes()),model,ConstantValue.CONCAT);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf btf = (ByteBuf) msg;
        Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        SessionUtils.SessionModel model =  SessionUtils
                .parserSessionId(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());
        if(target == null || !target.isActive()){
            createConnect(ctx,btf,model,ConstantValue.FORWARD);
        }else{
            sendMessage(model,target,btf,ConstantValue.FORWARD);
        }
    }

    private void createConnect(ChannelHandlerContext ctx,ByteBuf byteBuf,SessionUtils.SessionModel model,int type) {
        // 设置自动读取为关闭,什么时候打款自动读取呢？
        // 当桥接端 响应握手包之后 由  SentryClientHander 打开 AUTO_READ

        ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);
        clientBootstrap
                .connect(ServerConfig.INSTAND.getBridgeHost(), ServerConfig.INSTAND.getBridgePort())
                .addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        sendMessage(model, future.channel(), byteBuf,type)
                                .addListener((ChannelFutureListener) future1 -> {
                                    if(future1.isSuccess()){
                                        A2BUtils.addMapping(ctx.channel(),future.channel());
                                        //ctx.channel().config().setOption(ChannelOption.AUTO_READ,true);
                                    }else{
                                        ctx.close();
                                    }
                                });
                    }else{
                        ctx.close();
                        ReferenceCountUtil.release(byteBuf);
                    }
                });
    }





    /**
     * 发送FORWARD数据
     * @param model
     * @param channel
     * @param byteBuf
     */
    private ChannelFuture sendMessage(SessionUtils.SessionModel model, Channel channel, ByteBuf byteBuf,int type) {
        // log.info("byteBufAllocator 发送数据 == {} byte",byteBufAllocator.DEFAULT);
        log.info("SentryServerHander 发送数据 == {} byte",byteBuf.readableBytes());
        return   channel.writeAndFlush(CustomProtocolConverByteBuf.getByteBuf(new CustomProtocol(
                type,
                model.getSessionId().getBytes().length,
                model.getSessionId(),
                byteBuf.readableBytes(),
                byteBuf
        ),channel.alloc()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SessionUtils.SessionModel model =  SessionUtils
                .parserSessionId(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());
        if(null != model){
            Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
            // 向桥接端发送关闭事件 数据
            sendMessage(model,
                    target,
                    Unpooled.wrappedBuffer("CLOSE".getBytes()),ConstantValue.CLOSE)
            .addListener((ChannelFutureListener) channelFuture -> {
                log.info("哨兵服务端关闭事件 发送关闭消息 {}",channelFuture.isSuccess());
                target.close();
            }) ;
        }
        A2BUtils.removeMapping(ctx.channel());
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}