package com.haojiangbo.protocol;

import com.haojiangbo.codec.CustomProtocolDecoder;
import com.haojiangbo.codec.CustomProtocolEncoder;
import com.haojiangbo.config.RecvByteBufAllocatorCofigParSet;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.protocol.codec.MysqlAuthHander;
import com.haojiangbo.protocol.codec.MysqlProtocolDecode;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MySqlServer implements Container {

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    ServerBootstrap serverBootstrap = new ServerBootstrap();


    public static void  main(String [] args){
        MySqlServer mySqlServer = new MySqlServer();
        mySqlServer.start();
    }

    @Override
    public void start() {
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 添加协议的编解码工具
                        ch.pipeline().addLast(new MysqlProtocolDecode());
                        ch.pipeline().addLast(new MysqlAuthHander());
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, ConstantValue.SO_BACKLOG_VALUE)
                //.option(ChannelOption.SO_REUSEADDR, true)
                //.childOption(ChannelOption.SO_KEEPALIVE, true)
                .bind(13306).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.info("MYSQL服务器 ... 监听端口...{}",13306);
            }else{
                log.error("MYSQL服务器启动失败...");
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
