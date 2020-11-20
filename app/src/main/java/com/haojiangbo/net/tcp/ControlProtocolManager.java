package com.haojiangbo.net.tcp;

import android.util.Log;

import com.haojiangbo.net.codec.tcp.MediaProtocolDecode;
import com.haojiangbo.net.codec.tcp.MediaProtocolEncode;
import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.tcp.hander.ControProtocolHander;
import com.haojiangbo.net.tcp.hander.IdleCheckHandler;

import java.util.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 控制协议
 */
public class ControlProtocolManager extends ChannelInboundHandlerAdapter {
    public volatile static Channel ACTIVITY_CHANNEL = null;
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Bootstrap bridgeBootstrap;
    public volatile boolean IS_RESTART = true;


    private void init() {
        if (null != bridgeBootstrap) {
            return;
        }
        bridgeBootstrap = new Bootstrap();
        bridgeBootstrap.
                group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 添加自定义协议的编解码工具
                        ch.pipeline().addLast(new MediaProtocolDecode());
                        ch.pipeline().addLast(new MediaProtocolEncode());
                        //设置一个读取过期时间 和 写入过期时间  写入过期时间触发后 回向服务端写ping数据，如果规定时间内没有发生读事件，那么就是认为服务被停止
                        ch.pipeline().addLast(ControlProtocolManager.this);
                        ch.pipeline().addLast(new ControProtocolHander());
                        ch.pipeline().addLast(new IdleCheckHandler(IdleCheckHandler.READ_IDLE_TIME, IdleCheckHandler.WRITE_IDLE_TIME, 0));
                    }
                });
    }

    public void start() {
        init();
        bridgeBootstrap.
                connect(NettyKeyConfig.getHOST(),
                        NettyKeyConfig.getPORT())
                .addListener((ChannelFutureListener) future -> {
                    if(future.isSuccess()){
                        Log.e("net>>>","连接成功...");
                        ACTIVITY_CHANNEL = future.channel();
                        IdleCheckHandler.sendPingMessage(future.channel());
                    }else{
                        Log.e("net>>>","连接失败...");
                        ControlProtocolManager.this.restart();
                    }
                });
    }

    public void restart() {
        Log.e("net>>>", "准备尝试重新连接...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!this.IS_RESTART) {
            Log.e("net>>>", "事件循环组已关闭，无法重新启动");
            return;
        }
        ControlProtocolManager controlProtocolManager = new ControlProtocolManager();
        controlProtocolManager.start();
    }

    public void stop() {
        workerGroup.shutdownGracefully();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(NettyKeyConfig.SESSION_KEY).set(UUID.randomUUID().toString());
        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e("net>>>","连接断开...");
        super.channelInactive(ctx);
        ControlProtocolManager.this.stop();
        ControlProtocolManager.this.restart();
    }
}
