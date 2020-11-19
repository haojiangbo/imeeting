package com.haojiangbo.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class NettpUdpClientUtils {


    public static String HOST = "10.10.10.218";
    public static int PORT = 10086;
    public static Channel CHANNEL = null;

    public static void init(){
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                //.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                .handler(new ChannelInitializer<NioDatagramChannel>(){
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                        //nioDatagramChannel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        //nioDatagramChannel.pipeline().addLast(new UdpClientMessageReaderHander());
                    }
                });
        CHANNEL =  bootstrap.bind(10087).channel();
       /* String message = "my is client";
        ByteBuf byteBuf =  CHANNEL.config().getAllocator().directBuffer(message.getBytes().length);
        byteBuf.writeBytes(message.getBytes());
        DatagramPacket datagramPacket = new DatagramPacket(byteBuf,new InetSocketAddress(HOST,PORT));
        CHANNEL.writeAndFlush(datagramPacket);*/
    }


}
