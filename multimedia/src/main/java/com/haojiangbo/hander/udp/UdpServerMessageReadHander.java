package com.haojiangbo.hander.udp;

import com.haojiangbo.mapping.CallNumberAndChannelMapping;
import com.haojiangbo.protocol.MediaDataProtocol;
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

@Slf4j
public class UdpServerMessageReadHander extends ChannelInboundHandlerAdapter {
    File file = new File("D:/video/udp.mp3");
    int totalByte = 0;
    OutputStream outputStream;
   /* public UdpServerMessageReadHander(){
        if(file.exists()){
            file.delete();
        }
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
       /* StringBuilder stringBuilder = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(stringBuilder, datagramPacket.content());
        log.info(">>> ip >>> {} \n  {}",
                datagramPacket.sender(),
                stringBuilder.toString());*/
        int totalSize = datagramPacket.content().readableBytes();
        MediaDataProtocol protocol = MediaDataProtocol.byteBufToMediaDataProtocol(datagramPacket.content());
        ReferenceCountUtil.release(msg);
        String number = new String(protocol.number);
        switch (protocol.type) {
            case MediaDataProtocol.PING:
                InetSocketAddress address  =  datagramPacket.sender();
                log.info("ping message number = {}", number);
                // todo 此处未做删除的操作,会缓存一些无用数据
                CallNumberAndChannelMapping.UDP_NUMBER_CHANNEL_MAPPING.put(number,address);
                break;
            case MediaDataProtocol.VIDEO_DATA:
                /*log.info("ip {} 数据长度 {} 负载大小 {}",
                        datagramPacket.sender(),
                        totalSize,protocol.dataSize);
                ctx.writeAndFlush(new DatagramPacket( MediaDataProtocol.
                        mediaDataProtocolToByteBuf(ctx.channel(),protocol),new InetSocketAddress("127.0.0.1", 10089)));*/
                mediaProtocolForword(ctx, datagramPacket, totalSize, protocol, number);
                break;
            case MediaDataProtocol.AUDIO_DATA:
                mediaProtocolForword(ctx, datagramPacket, totalSize, protocol, number);
                break;
        }


        //ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(),datagramPacket.sender()));
        //ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(),new InetSocketAddress("192.168.43.208", 10089)));
        //ctx.writeAndFlush(new DatagramPacket(datagramPacket.content(), new InetSocketAddress("10.10.10.234", 10089)));
        /*try {
            byte [] b = new byte[datagramPacket.content().readableBytes()];
            datagramPacket.content().readBytes(b);
            outputStream.write(b);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void mediaProtocolForword(ChannelHandlerContext ctx, DatagramPacket datagramPacket, int totalSize, MediaDataProtocol protocol, String number) {
        log.info("ip {} 数据长度 {} 负载大小 {}",
                datagramPacket.sender(),
                totalSize,protocol.dataSize);
        InetSocketAddress ar =  CallNumberAndChannelMapping.UDP_NUMBER_CHANNEL_MAPPING.get(number);
        if(null != ar){
            ctx.writeAndFlush(new DatagramPacket(
                    MediaDataProtocol.
                            mediaDataProtocolToByteBuf(ctx.channel(),protocol)
                    , ar));
        }
    }
}
