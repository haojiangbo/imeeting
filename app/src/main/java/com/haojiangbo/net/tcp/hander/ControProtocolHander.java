package com.haojiangbo.net.tcp.hander;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.eventbus.CallReplyModel;
import com.haojiangbo.eventbus.MessageModel;
import com.haojiangbo.ndkdemo.MainActivity;
import com.haojiangbo.net.protocol.ControlProtocol;
import com.haojiangbo.net.protocol.Pod;
import com.haojiangbo.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 媒体控制协议处理
 */
public class ControProtocolHander extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ControlProtocol protocol = (ControlProtocol) msg;
        String data = new String(protocol.data);
        replyMessage(ctx, protocol.flag, data);
       /* switch (protocol.flag) {
            case ControlProtocol.CREATE_REPLAY:
                // 回复接通的消息 通过eventBus 传输到 mainActivity
                replyMessage(ctx, protocol.flag, data);
                break;
            case ControlProtocol.JOIN_REPLAY:
                replyMessage(ctx, protocol.flag, data);
                break;
            case ControlProtocol.CLOSE:
                replyMessage(ctx, protocol.flag, data);
                break;
        }*/
    }

    private void replyMessage(ChannelHandlerContext ctx, byte type, String data) {
        EventBus.getDefault().post(new MessageModel(type, data, ctx.channel()));
    }


}
