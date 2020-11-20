package com.haojiangbo.server;

import com.haojiangbo.codec.tcp.MediaProtocolDecode;
import com.haojiangbo.codec.tcp.MediaProtocolEncode;
import com.haojiangbo.config.ServcerConfig;
import com.haojiangbo.hander.tcp.ControProtocolHander;
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
public class TcpServerApp {
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private  EventLoopGroup workerGroup = new NioEventLoopGroup(ServcerConfig.WORK_THREAD_NUM / 2);
    ServerBootstrap serverBootstrap = null;

    public void start(int port){
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        //RecvByteBufAllocatorCofigParSet.set(ch);
                        ch.pipeline().addLast(new MediaProtocolDecode());
                        ch.pipeline().addLast(new MediaProtocolEncode());
                        //协议解析
                        ch.pipeline().addLast(new ControProtocolHander());
                    }
                })
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                .bind(port).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.info("控制协议服务器启动成功...TCP监听端口...{}",port);
            }else{
                log.error("控制协议服务器启动失败..TCP监听端口...{}",port);
            }
        });
    }

    public void stop() {
        // 优雅关闭
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
