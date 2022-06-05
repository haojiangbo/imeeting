package com.haojiangbo.net.hander;

import android.util.Log;

import com.haojiangbo.net.protocol.MediaDataProtocol;
import com.haojiangbo.thread.MeidaParserInstand;
import com.haojiangbo.thread.ParserMediaProtoThreadPool;
import com.haojiangbo.thread.VideoMediaParserInstand;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class MediaProtocolHander extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
       /* StringBuilder stringBuffer = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(stringBuffer,datagramPacket.content());*/
        //Log.e("HJB",stringBuffer.toString());
        Log.i("数据大小>>", datagramPacket.content().readableBytes() + ">>>>");
        byte type = datagramPacket.content().readByte();
        if (VideoMediaParserInstand.THREAD_STATE != 1) {
            return;
        }
        switch (type) {
            case MediaDataProtocol.AUDIO_DATA:
                MeidaParserInstand.MEDIA_DATA_QUEUE.put(datagramPacket.content());
                break;
            case MediaDataProtocol.VIDEO_DATA:
                VideoMediaParserInstand.MEDIA_DATA_QUEUE.put(datagramPacket.content());
                break;
        }

        //super.channelRead(ctx, msg);
    }
}
