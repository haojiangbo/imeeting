package com.haojiangbo.container;

import com.haojiangbo.codec.CustomProtocolDecoder;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.hander.SentryClientHander;
import com.haojiangbo.hander.SentryServerHander;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.model.ConfigModel;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
  *
  * 哨兵容器
 　　* @author 郝江波
 　　* @date 2020/4/16 9:19
 　　*/
@Slf4j
public class SentryEngineContainer implements Container {

    private  EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private  EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap serverBootstrap = null;
    Bootstrap  clientBootstrap = null;

    public void start(int port,String clientId,String url){


        clientBootstrap = new Bootstrap();
        clientBootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new CustomProtocolDecoder());
                        ch.pipeline().addLast(new SentryClientHander());
                    }
                });


        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new SentryServerHander(clientId,clientBootstrap,url));
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(port).addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        log.info("哨兵服务引擎启动成功... 监听端口...{}",port);
                    }else{
                        log.error("哨兵服务引擎启动失败...");
                    }
                });
    }

    @Override
    public void start() {
        List<ConfigModel> configModelList =  ServerConfig.INSTAND.getConfigList();
        for(ConfigModel item : configModelList){
            start(item.getPort(),item.getClientId(),item.getClientUrl());
        }
    }
    @Override
    public void stop() {
        // 优雅关闭
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
