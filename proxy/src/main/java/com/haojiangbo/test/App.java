package com.haojiangbo.test;

import com.haojiangbo.hander.ProxyClientHander;
import com.haojiangbo.hander.ProxyHander;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 　　* @author 郝江波
 * 　　* @date 2020/4/15 11:59
 */
@Slf4j
public class App {
    /*@SneakyThrows
    public static void main(String[] argg) {
        // 配置NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);


        *//**
         * 客户端group
         *//*
        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProxyClientHander());
                    }
                });


        try {
            // 服务器辅助启动类配置
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 处理网络IO
                            ch.pipeline().addLast(new ProxyHander(clientBootstrap));
                        }
                    })
                    // 设置tcp缓冲区 // (5)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口 同步等待绑定成功
            ChannelFuture f = b.bind(666).sync();
            // 等到服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅释放线程资源
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }*/
}
