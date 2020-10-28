package com.haojiangbo.udp;

import com.haojiangbo.hander.udp.UdpClientMessageReaderHander;
import com.haojiangbo.hander.udp.UdpServerMessageReadHander;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
  * udp协议服务器
 　　* @author 郝江波
 　　* @date 2020/10/15 10:54
 　　*/
@Slf4j
public class UdpClientApp {



    public static void main(String [] ages) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                .handler(new ChannelInitializer<NioDatagramChannel>(){
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                        //nioDatagramChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        nioDatagramChannel.pipeline().addLast(new UdpClientMessageReaderHander());
                    }
                });
        Channel channel =  bootstrap.bind(10087).channel();
        String message = "my is client";
        ByteBuf byteBuf =  channel.config().getAllocator().directBuffer(message.getBytes().length);
        byteBuf.writeBytes(message.getBytes());
        DatagramPacket datagramPacket = new DatagramPacket(byteBuf,new InetSocketAddress("127.0.0.1",10086));
        channel.writeAndFlush(datagramPacket);
        log.info("发送成功", 1);
    }

}
