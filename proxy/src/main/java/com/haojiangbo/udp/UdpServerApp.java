package com.haojiangbo.udp;

import com.haojiangbo.hander.udp.UdpServerMessageReadHander;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

/**
  * udp协议服务器
 　　* @author 郝江波
 　　* @date 2020/10/15 10:54
 　　*/
@Slf4j
public class UdpServerApp {


    public static void main(String [] ages) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                .handler(new ChannelInitializer<NioDatagramChannel>(){
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                        nioDatagramChannel.pipeline().addLast(new UdpServerMessageReadHander());
                    }
                });
        bootstrap.bind(10086).sync().await();
    }

}
