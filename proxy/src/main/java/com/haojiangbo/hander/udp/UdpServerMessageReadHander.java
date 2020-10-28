package com.haojiangbo.hander.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class UdpServerMessageReadHander extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket  datagramPacket = (DatagramPacket) msg;
        log.info("接收到客户端消息 >>> {} ip >>> {}",datagramPacket.content().toString(Charset.forName("utf-8")),datagramPacket.sender());
        ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(),datagramPacket.sender()));
    }
}
