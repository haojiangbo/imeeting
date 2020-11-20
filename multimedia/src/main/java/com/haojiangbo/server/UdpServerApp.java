package com.haojiangbo.server;

import com.haojiangbo.config.ServcerConfig;
import com.haojiangbo.hander.udp.UdpServerMessageReadHander;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * udp协议服务器
 * 　　* @author 郝江波
 * 　　* @date 2020/10/15 10:54
 *
 */
@Slf4j
public class UdpServerApp {


    public static void main(String[] ages) {
        TcpServerApp tcpServerApp = new TcpServerApp();
        tcpServerApp.start(ServcerConfig.LISTENER_PROT);

        UdpServerApp udpServerApp = new UdpServerApp();
        udpServerApp.start(ServcerConfig.LISTENER_PROT);
    }

    public void start(int port) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                        nioDatagramChannel.pipeline().addLast(new UdpServerMessageReadHander());
                    }
                });
        bootstrap.bind(ServcerConfig.LISTENER_PROT).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("数据转发服务器启动成功...UDP监听端口...{}", port);
            } else {
                log.error("数据转发服务器启动失败..UDP监听端口...{}",port);
            }
        });
    }

}
