package com.haojiangbo.net.hander;

import android.util.Log;

import com.haojiangbo.thread.MeidaParserInstand;
import com.haojiangbo.thread.ParserMediaProtoThreadPool;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class MediaProtocolHander  extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DatagramPacket datagramPacket = (DatagramPacket) msg;
        StringBuilder stringBuffer = new StringBuilder();
        ByteBufUtil.appendPrettyHexDump(stringBuffer,datagramPacket.content());
        //Log.e("HJB",stringBuffer.toString());
        Log.e("数据大小>>",datagramPacket.content().readableBytes() + ">>>>");
        MeidaParserInstand.MEDIA_DATA_QUEUE.put(datagramPacket.content());
        //super.channelRead(ctx, msg);
    }
}
