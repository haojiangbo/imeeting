package com.haojiangbo.net;

import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.hander.MediaProtocolHander;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class MediaProtocolManager {

    public static Channel CHANNEL = null;
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    public void start(){
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                .handler(new ChannelInitializer<NioDatagramChannel>(){
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                        nioDatagramChannel.pipeline().addLast(new MediaProtocolHander());
                    }
                });
        CHANNEL =  bootstrap.bind(NettyKeyConfig.getPORT()).channel();
    }

    public void stop(){
        workerGroup.shutdownGracefully();
    }
}
