package com.haojiangbo.hander.udp;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

@Slf4j
public class UdpServerMessageReadHander extends ChannelInboundHandlerAdapter {
    File file = new File("D:/video/udp.pcm");
    public UdpServerMessageReadHander(){
        if(file.exists()){
            file.delete();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket  datagramPacket = (DatagramPacket) msg;
        StringBuilder stringBuilder = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(stringBuilder,datagramPacket.content());
        log.info("接收到客户端消息 >>> {} ip >>> {} \n  {}",datagramPacket.content().toString(Charset.forName("utf-8")),datagramPacket.sender(),stringBuilder.toString());
        //ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(),datagramPacket.sender()));
        try {
            byte [] b = new byte[datagramPacket.content().readableBytes()];
            datagramPacket.content().readBytes(b);
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(b);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
