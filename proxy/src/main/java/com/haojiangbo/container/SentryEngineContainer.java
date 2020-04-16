package com.haojiangbo.container;

import com.haojiangbo.codec.CustomProtocolDecoder;
import com.haojiangbo.codec.CustomProtocolEncoder;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.hander.BrigdeHander;
import com.haojiangbo.hander.SentryHander;
import com.haojiangbo.inteface.Container;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
  *
  * 哨兵容器
 　　* @author 郝江波
 　　* @date 2020/4/16 9:19
 　　*/
@Slf4j
public class SentryEngineContainer implements Container {

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap serverBootstrap = null;


    public void start(int port,String clientId){
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new SentryHander(clientId));
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        serverBootstrap.bind(port).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.info("哨兵服务引擎启动成功... 监听端口...{}",port);
            }else{
                log.error("哨兵服务引擎启动失败...");
            }
        });
    }

    @Override
    public void start() {
        start(777,"123");
    }

    @Override
    public void stop() {

    }
}
