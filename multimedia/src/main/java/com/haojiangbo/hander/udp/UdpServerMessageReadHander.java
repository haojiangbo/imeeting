package com.haojiangbo.hander.udp;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

@Slf4j
public class UdpServerMessageReadHander extends ChannelInboundHandlerAdapter {
    File file = new File("D:/video/udp.mp3");
    int totalByte = 0;
    OutputStream outputStream;
    public UdpServerMessageReadHander(){
        if(file.exists()){
            file.delete();
        }
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket  datagramPacket = (DatagramPacket) msg;
        StringBuilder stringBuilder = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(stringBuilder,datagramPacket.content());
        totalByte += datagramPacket.content().readableBytes();
        log.info(">>> ip >>> {} byteLen = {} totalLen = {}  \n  {}",
                datagramPacket.sender(),
                datagramPacket.content().readableBytes(),
                totalByte,
                stringBuilder.toString());
        //ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(),datagramPacket.sender()));
        //ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(),new InetSocketAddress("192.168.43.208", 10089)));
        ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(),new InetSocketAddress("10.10.10.234", 10089)));
        /*try {
            byte [] b = new byte[datagramPacket.content().readableBytes()];
            datagramPacket.content().readBytes(b);
            outputStream.write(b);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }
}
