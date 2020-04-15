package com.haojiangbo.hander;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.model.CustomProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHander extends ChannelInboundHandlerAdapter {

    private static Bootstrap bootstrap ;

    public ClientHander(Bootstrap btp){
        bootstrap = btp;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 连接服务端
       /* bootstrap.connect(ClientConfig.INSTAND.getLocalHost(),ClientConfig.INSTAND.getLocalPort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {

                    }
                });*/
        CustomProtocol msge = (CustomProtocol) msg;

        // byte array 转  byteBuf
        // ByteBuf byteBuf =  Unpooled.wrappedBuffer(message.getContent());
        switch (msge.getMeesgeType()){
            case ConstantValue.PING:
                pingHander(ctx,msge);
                break;
        }
        super.channelRead(ctx,msg);
    }


    private  void  pingHander(ChannelHandlerContext ctx,CustomProtocol message){
        log.info("收到服务器的回复消息  clientId = {}",message.getClientId());
    }

}
