package com.haojiangbo.container;

import com.haojiangbo.codec.CustomProtocolDecoder;
import com.haojiangbo.codec.CustomProtocolEncoder;
import com.haojiangbo.config.BrigdeChannelMapping;
import com.haojiangbo.config.ClientCheckConfig;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.hander.BrigdeHander;
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
  * 这是一个连接桥梁的server
  * 客户端主动与该server连接
  * 通过客户端携带的key值
  * 实现数据交
 　　* @author 郝江波
 　　* @date 2020/4/15 16:03
 　　*/
@Slf4j
public class BridgeEngineContainner implements Container {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap serverBootstrap = new ServerBootstrap();


    @Override
    public void start() {

        BrigdeChannelMapping.CLIENT_ID_MAPPING.clear();
        BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.clear();

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
                        ch.pipeline().addLast(new BrigdeHander());
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(ServerConfig.INSTAND.getBridgePort()).addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        log.info("桥梁服务引擎启动成功... 监听端口...{}",ServerConfig.INSTAND.getBridgePort());
                    }else{
                        log.error("桥梁服务引擎启动失败...");
                    }
                });
    }

    @Override
    public void stop() {
        // 优雅关闭
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
