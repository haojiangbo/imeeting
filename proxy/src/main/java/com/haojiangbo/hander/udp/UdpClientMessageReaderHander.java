package com.haojiangbo.hander.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
  * udp客户端接收消息提醒
 　　* @author 郝江波
 　　* @date 2020/10/15 11:56
 　　*/
 @Slf4j
public class UdpClientMessageReaderHander extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        log.info("接收到服务端的消息 >>> {} address >>> {}",datagramPacket.content().toString(Charset.forName("UTF-8")),datagramPacket.sender());
    }
}
