package com.haojiangbo.container;

import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.hander.ProxyClientHander;
import com.haojiangbo.hander.ProxyHander;
import com.haojiangbo.inteface.Container;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
  *     反向代理启动类
 　　* @author 郝江波
 　　* @date 2020/4/15 15:46
 　　*/
@Slf4j
public class ProxyEngineContainer implements Container {


    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup(4);
    Bootstrap clientBootstrap = new Bootstrap();
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    public void initServer(int port){
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 处理网络IO
                        ch.pipeline().addLast(new ProxyHander(clientBootstrap));
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.bind(ServerConfig.INSTAND.getProxyPort()).addListener((ChannelFutureListener) future -> {
                if(future.isSuccess()){
                    log.info("反向代理引擎启动成功... 监听端口...{}",ServerConfig.INSTAND.getProxyPort());
                }else{
                    log.error("反向代理引擎启动失败...");
                }
            });
    }

    public void initClient(){
        /**
         * 客户端group
         */
        clientBootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ProxyClientHander());
                    }
                });
    }


    @Override
    public void start() {
        initClient();
        initServer(666);
    }

    @Override
    public void stop() {
        // 优雅释放线程资源
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
