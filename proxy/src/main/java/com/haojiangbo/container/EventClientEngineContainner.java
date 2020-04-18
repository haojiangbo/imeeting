package com.haojiangbo.container;

import com.haojiangbo.codec.CustomProtocolDecoder;
import com.haojiangbo.codec.CustomProtocolEncoder;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.hander.EventClientHander;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.shell.CmdShellHander;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
    *  shell 客户端命令处理
 　　* @author 郝江波
 　　* @date 2020/4/18 11:36
 　　*/
public class EventClientEngineContainner implements Container {
    private EventLoopGroup workerGroup = new NioEventLoopGroup(1);
    private   Bootstrap clientBootstrap ;

    private void init(){
        clientBootstrap = new Bootstrap();
        clientBootstrap.
                group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 添加自定义协议的编解码工具
                        ch.pipeline().addLast(new CustomProtocolEncoder());
                        ch.pipeline().addLast(new CustomProtocolDecoder());
                        ch.pipeline().addLast(new EventClientHander(EventClientEngineContainner.this));
                    }
                });
    }


    @Override
    public void start() {
        init();
        clientBootstrap.connect("127.0.0.1", ServerConfig.INSTAND.getEventPort())
                .addListener((ChannelFutureListener) future -> {
                    if(!future.isSuccess()){
                        CmdShellHander.println("连接失败...");
                        restart();
                    }
                });
    }



    public void restart() {
        CmdShellHander.println("正在重新连接...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        start();
    }


    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }
}
