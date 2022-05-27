package com.haojiangbo.hander.udp;

import com.haojiangbo.mapping.CallNumberAndChannelMapping;
import com.haojiangbo.protocol.MediaDataProtocol;
import com.haojiangbo.utils.SessionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class UdpServerMessageReadHander extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        int totalSize = datagramPacket.content().readableBytes();
        MediaDataProtocol protocol = MediaDataProtocol.byteBufToMediaDataProtocol(datagramPacket.content());
        ReferenceCountUtil.release(msg);
        String number = new String(protocol.number);
        SessionUtils.Model model =  SessionUtils.splitSession(number);
        switch (protocol.type) {
            case MediaDataProtocol.PING:
                log.info("udp session = {}", number);
                InetSocketAddress address = datagramPacket.sender();
                Map umap = CallNumberAndChannelMapping.UDP_ROOM_CHANNEL_MAPPING.get(model.key);
                if (null == umap) {
                    umap = new ConcurrentHashMap();
                    CallNumberAndChannelMapping.UDP_ROOM_CHANNEL_MAPPING.put(model.key, umap);
                }
                umap.put(model.uid, address);
                break;
            case MediaDataProtocol.VIDEO_DATA:
                mediaProtocolForward(ctx, datagramPacket, totalSize, protocol, model.key);
                break;
            case MediaDataProtocol.AUDIO_DATA:
                mediaProtocolForward(ctx, datagramPacket, totalSize, protocol, model.key);
                break;
        }

    }

    private void mediaProtocolForward(ChannelHandlerContext ctx, DatagramPacket datagramPacket, int totalSize, MediaDataProtocol protocol, String number) {
        String tmpSession = new String(protocol.number);
        if(!CallNumberAndChannelMapping.SESSION_CHANNEL_MAPPING.containsKey(tmpSession)){
            return;
        }
        log.info("ip {} 数据长度 {} 负载大小 {}",
                datagramPacket.sender(),
                totalSize, protocol.dataSize);
        Map<String, InetSocketAddress> ar = CallNumberAndChannelMapping.UDP_ROOM_CHANNEL_MAPPING.get(number);
        // 向所有连接的udp端发送视频数据
        if (null != ar) {
            ar.keySet().stream().forEach(item -> ctx.writeAndFlush(new DatagramPacket(
                    MediaDataProtocol.
                            mediaDataProtocolToByteBuf(ctx.channel(), protocol)
                    , ar.get(item))));
        }
    }
}
