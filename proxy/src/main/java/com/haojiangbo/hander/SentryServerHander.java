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
    public SentryServerHander(String clientId, Bootstrap bootstrap, String clientUrl){
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
                .connect(ServerConfig.INSTAND.getBridgeHost(), ServerConfig.INSTAND.getBridgePort())
                .addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        A2BUtils.addMapping(ctx.channel(),future.channel());
                        sendForwardMessage(model, future.channel(), byteBuf)
                                .addListener((ChannelFutureListener) future1 -> {
                                    if(future1.isSuccess()){
                                        ctx.channel().config().setOption(ChannelOption.AUTO_READ,true);
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
    private ChannelFuture sendForwardMessage(SessionUtils.SessionModel model, Channel channel, ByteBuf byteBuf) {
      return   channel.writeAndFlush(CustomProtocolConverByteBuf.getByteBuf(new CustomProtocol(
                ConstantValue.FORWARD,
                model.getSessionId().getBytes().length,
                model.getSessionId(),
                byteBuf.readableBytes(),
                byteBuf
        )));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        A2BUtils.removeMapping(ctx.channel());
        super.channelInactive(ctx);
    }

}
