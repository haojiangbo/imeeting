package com.haojiangbo.hander;

import com.haojiangbo.config.BrigdeChannelMapping;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.config.SessionChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.model.CustomProtocolConverByteBuf;
import com.haojiangbo.utils.AtoBUtils;
import com.haojiangbo.utils.SessionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
  * 哨兵数据处理器
 　　* @author 郝江波
 　　* @date 2020/4/16 13:42
 　　*/
 @Slf4j
public class SentryHander extends ChannelInboundHandlerAdapter {
    private String clientId = null;
    private Bootstrap clientBootstrap = null;
    private String clientUrl = null;
    public SentryHander(String clientId,Bootstrap bootstrap,String clientUrl){
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
        //添加映射关系
        // SessionChannelMapping.SESSION_CHANNEL_MAPPING.put(sessionId, ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf btf = (ByteBuf) msg;
        Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        SessionUtils.SessionModel model =  SessionUtils
                .parserSessionId(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());
        if(target == null || !target.isActive()){
            createConnect(ctx,btf,model);
        }else{
            sendForwardMessage(model,target,btf);
        }
    }

    private void createConnect(ChannelHandlerContext ctx,ByteBuf byteBuf,SessionUtils.SessionModel model) {
        ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);
        clientBootstrap
                .connect("127.0.0.1", 10009)
                .addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        AtoBUtils.addMapping(ctx.channel(),future.channel());
                        sendForwardMessage(model, future.channel(), byteBuf);
                    }else{
                        ctx.close();
                    }
                    ctx.channel().config().setOption(ChannelOption.AUTO_READ,true);
                });
    }

    /**
     * 发送FORWARD数据
     * @param model
     * @param channel
     * @param byteBuf
     */
    private void sendForwardMessage(SessionUtils.SessionModel model, Channel channel, ByteBuf byteBuf) {
        byte[] result = ByteBufUtil.getBytes(byteBuf);
        channel.writeAndFlush(CustomProtocolConverByteBuf.getByteBuf(new CustomProtocol(
                ConstantValue.FORWARD,
                Integer.valueOf(model.getClientId()),
                model.getSessionId().getBytes().length,
                model.getSessionId(),
                result.length,
                result
        )));
        //释放引用
        ReferenceCountUtil.release(byteBuf);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        /*log.info("channelId = {} 断开连接",ctx.channel().id().asShortText());*/
        AtoBUtils.removeMapping(ctx.channel());
        super.channelInactive(ctx);
    }


    /*@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf btf = (ByteBuf) msg;

        //找出客户端和服务端的通道
        SessionUtils.SessionModel model =  SessionUtils
                .parserSessionId(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());
        Channel target =  BrigdeChannelMapping.CLIENT_ID_MAPPING.get(model.getClientId());
        byte[] result = ByteBufUtil.getBytes(btf);


        //通过中间通道 向客户端链接发送消息
        if(target != null && target.isActive()){
            //向客户端发送消息
            target.writeAndFlush(new CustomProtocol(
                    ConstantValue.DATA,
                    Integer.valueOf(model.getClientId()),
                    model.getSessionId().getBytes().length,
                    model.getSessionId(),
                    result.length,
                    result
            ));
            ReferenceCountUtil.release(btf);
        }else{
            log.info("没有发现活跃通道,已关闭链接");
            ctx.close();
        }
    }*/


   /* @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SessionChannelMapping.SESSION_CHANNEL_MAPPING.remove(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());
        super.channelInactive(ctx);
    }*/

}
