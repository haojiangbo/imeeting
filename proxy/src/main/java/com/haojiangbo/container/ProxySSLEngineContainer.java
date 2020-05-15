package com.haojiangbo.container;

import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.hander.ProxyClientHander;
import com.haojiangbo.hander.ProxyServerHander;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.ssl.HttpSslContextFactory;
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
import io.netty.handler.ssl.SslHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 *
 * https://blog.csdn.net/huanongdetian/article/details/80175899?depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-1&utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-1
 * jdk 生成 密钥
 * keytool -genkey -alias netty -keypass 123456 -keyalg RSA -keysize 1024 -validity 365 -keystore D:/netty.keystore -storepass 123456
 *
 *
  *     反向代理启动类
 　　* @author 郝江波
 　　* @date 2020/4/15 15:46
 　　*/
@Slf4j
public class ProxySSLEngineContainer implements Container {


    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup(4);
    Bootstrap clientBootstrap = new Bootstrap();
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    public void initServer(){
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {


                        // =====================以下为SSL处理代码=================================

                        // 暂时只支持阿里云的免费 单域名  pfx 证书

                        ch.pipeline().addFirst("ssl",
                                new SslHandler(HttpSslContextFactory
                                        .createSslEngine(
                                                ServerConfig.INSTAND.getKeyStoryPath(),
                                                ServerConfig.INSTAND.getKeyStoryPassword()
                                        )
                                ));

                        // =====================以上为SSL处理代码=================================

                        // 处理网络IO
                        ch.pipeline().addLast(new ProxyServerHander(clientBootstrap));
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(ServerConfig.INSTAND.getProxySSLPort()).addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        log.info("反向代理引擎启动成功... 监听端口...{}",ServerConfig.INSTAND.getProxySSLPort());
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
        initServer();
    }

    @Override
    public void stop() {
        // 优雅释放线程资源
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
