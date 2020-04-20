package com.haojiangbo.container;

import com.haojiangbo.codec.CustomProtocolDecoder;
import com.haojiangbo.codec.CustomProtocolEncoder;
import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.hander.ClientHander;
import com.haojiangbo.hander.ClientProxyHander;
import com.haojiangbo.hander.IdleCheckHandler;
import com.haojiangbo.inteface.Container;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
* @Title: BridgeClientContainer
* @Package com.haojiangbo.container
* @Description: 代理桥梁客户端
* @author Administrator
* @date 2020/4/15
* @version V1.0
*/
@Slf4j
public class BridgeClientContainer implements Container {

    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Bootstrap clientBootstrap = new Bootstrap();
    private Bootstrap bridgeBootstrap ;
    public static volatile boolean IS_RESTART = true;


    private void init(){
        if(null !=  bridgeBootstrap){return;}

        clientBootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ClientProxyHander());
                    }
                });


        bridgeBootstrap = new Bootstrap();
        bridgeBootstrap.
                group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 添加自定义协议的编解码工具
                        ch.pipeline().addLast(new CustomProtocolEncoder());
                        ch.pipeline().addLast(new CustomProtocolDecoder());
                        //设置一个读取过期时间 和 写入过期时间  写入过期时间触发后 回向服务端写ping数据，如果规定时间内没有发生读事件，那么就是认为服务被停止
                        ch.pipeline().addLast(new IdleCheckHandler(IdleCheckHandler.READ_IDLE_TIME, IdleCheckHandler.WRITE_IDLE_TIME, 0));
                        ch.pipeline().addLast(new ClientHander(clientBootstrap,BridgeClientContainer.this));
                    }
                });
    }

    @Override
    public void start() {
        init();
        bridgeBootstrap.
                connect(ClientConfig.INSTAND.getRemoteHost(),
                        ClientConfig.INSTAND.getRemotePort())
                .addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        log.info("连接成功...");
                        IdleCheckHandler.sendPingMessage(future.channel());
                    }else{
                        restart();
                    }
                });
    }

    @SneakyThrows
    public void restart() {
        log.info("准备尝试重新连接...");
        Thread.sleep(3000);
        if(!BridgeClientContainer.IS_RESTART){
            log.error("事件循环组已关闭，无法重新启动");
            return;
        }
        start();
    }


    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
    }


}
