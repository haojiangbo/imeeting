package com.haojiangbo.hander;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.config.SessionChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.container.BridgeClientContainer;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.utils.SessionUtils;
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

        switch (message.getMeesgeType()){
            case ConstantValue.PING:
                pingHander(ctx,message);
                break;
            case ConstantValue.DATA:
                dataHander(ctx, message);
                break;
        }
    }

    private void dataHander(ChannelHandlerContext ctx, CustomProtocol message) {
        log.info("收到服务器端的数据消息.......{}", message.getSessionId());
        String sessionId =  message.getSessionId();
        Channel target =  SessionChannelMapping.SESSION_CHANNEL_MAPPING.get(sessionId);
        if(target == null){
            ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);
            createConnect(ctx,message);
        }else{
            target.writeAndFlush(message.getContent());
        }
        ReferenceCountUtil.release(message);
    }

    private void createConnect(ChannelHandlerContext ctx, CustomProtocol message) {
        // 连接服务端
        SessionUtils.SessionModel model =  SessionUtils.parserSessionId(message.getSessionId());
        bootstrap.connect(model.getHost(),model.getPort())
                .addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        //此处的sessionId 是为了方便转发数据的时候获取sessionId
                        future.channel().attr(NettyProxyMappingConstant.SESSION).set(message.getSessionId());
                        //关联当前管道
                        future.channel().attr(NettyProxyMappingConstant.MAPPING).set(ctx.channel());
                        //下次服务端再有数据的时候，不需要重新连接
                        SessionChannelMapping.SESSION_CHANNEL_MAPPING.put(message.getSessionId(), future.channel());
                    }
                    //向服务发送数据
                    future.channel().writeAndFlush(message.getContent());
                    ctx.channel().config().setOption(ChannelOption.AUTO_READ,true);
                });
    }


    private  void  pingHander(ChannelHandlerContext ctx,CustomProtocol message){
        if(log.isDebugEnabled()){
            log.debug("收到服务器的心跳消息  clientId = {}",message.getClientId());
        }
        //释放消息，防止内存泄漏
        ReferenceCountUtil.release(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        bridgeClientContainer.restart();
    }
}
