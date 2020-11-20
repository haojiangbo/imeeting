package com.haojiangbo.net.tcp.hander;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.net.protocol.ControlProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 媒体控制协议处理
 */
public class ControProtocolHander  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ControlProtocol protocol = (ControlProtocol) msg;
        switch (protocol.flag){
            case ControlProtocol.CALL:
                String data = new String(protocol.data);
                Log.e("net call",">>>>>>>"+data);
                break;
            case ControlProtocol.CALL_REPLY:
                break;
            case ControlProtocol.HANG:
                break;
        }
    }
}
