package com.haojiangbo.container;

import com.haojiangbo.codec.CustomProtocolDecoder;
import com.haojiangbo.codec.CustomProtocolEncoder;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.hander.BrigdeHander;
import com.haojiangbo.hander.EventServerHander;
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

/**
  *
  * 服务端事件处理器
 　　* @author 郝江波
 　　* @date 2020/4/18 10:53
 　　*/
 @Slf4j
public class EventEngineContainner implements Container {

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup(1);
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    public void  init(){
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 添加自定义协议的编解码工具
                        ch.pipeline().addLast(new CustomProtocolEncoder());
                        ch.pipeline().addLast(new CustomProtocolDecoder());
                        // 处理网络IO
                        ch.pipeline().addLast(new EventServerHander());
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(ServerConfig.INSTAND.getEventPort()).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.info("事件处理引擎启动成功... 监听端口...{}",ServerConfig.INSTAND.getEventPort());
            }else{
                log.error("事件处理引擎启动失败...");
            }
        });
    }


    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
