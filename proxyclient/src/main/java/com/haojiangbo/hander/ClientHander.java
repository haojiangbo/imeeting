package com.haojiangbo.hander;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.container.BridgeClientContainer;
import com.haojiangbo.model.CustomProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHander extends ChannelInboundHandlerAdapter {

    private static Bootstrap bootstrap ;
    private static BridgeClientContainer bridgeClientContainer;

    public ClientHander(Bootstrap btp,BridgeClientContainer bc){
        bootstrap = btp;
        bridgeClientContainer = bc;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomProtocol message = (CustomProtocol) msg;

        // byte array 转  byteBuf
        // ByteBuf byteBuf =  ;
        switch (message.getMeesgeType()){
            case ConstantValue.PING:
                pingHander(ctx,message);
                //释放消息，防止内存泄漏
                ReferenceCountUtil.release(message);
                break;
            case ConstantValue.DATA:
                dataHander(ctx, message);
                break;
        }
    }

    private void dataHander(ChannelHandlerContext ctx, CustomProtocol message) {
        Channel target = ctx.channel().attr(NettyProxyMappingConstant.MAPPING).get();
        if(target == null){
            ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);
            mapingHander(ctx, message);
        }else{
            target.writeAndFlush(Unpooled.wrappedBuffer(message.getContent()));
        }
    }

    private void mapingHander(ChannelHandlerContext ctx, CustomProtocol message) {
        // 连接服务端
        bootstrap.connect(ClientConfig.INSTAND.getLocalHost(),ClientConfig.INSTAND.getLocalPort())
                .addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        future.channel().attr(NettyProxyMappingConstant.MAPPING).set(ctx.channel());
                        ctx.channel().attr(NettyProxyMappingConstant.MAPPING).set(future.channel());
                        future.channel().writeAndFlush(Unpooled.wrappedBuffer(message.getContent()));
                    }
                    ctx.channel().config().setOption(ChannelOption.AUTO_READ,true);
                });
    }


    private  void  pingHander(ChannelHandlerContext ctx,CustomProtocol message){
        log.info("收到服务器的回复消息  clientId = {}",message.getClientId());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        bridgeClientContainer.restart();
    }
}
