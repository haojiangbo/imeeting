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
public class ControProtocolHander  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ControlProtocol protocol = (ControlProtocol) msg;
        String data = new String(protocol.data);
        switch (protocol.flag){
            case ControlProtocol.CALL:
                Log.e("net call",">>>>>>>"+data);
                // 回复接通的消息 通过eventBus 传输到 mainActivity
                replyMessage(ctx, protocol, data);
                break;
            // 接收到对方的回复消息
            case ControlProtocol.CALL_REPLY:
                EventBus.getDefault().post(new CallReplyModel(protocol));
                break;
            case ControlProtocol.HANG:
                break;
        }
    }

    private void replyMessage(ChannelHandlerContext ctx, ControlProtocol protocol, String data) {
        Pod pod =  JSONObject.parseObject(data, Pod.class);
        String tmpSrc = pod.getSrc();
        pod.setSrc(pod.getDst());
        pod.setDst(tmpSrc);
        byte [] re = pod.toString().getBytes();
        protocol.flag = ControlProtocol.CALL_REPLY;
        protocol.dataSize = re.length;
        protocol.data = re;
        MainActivity.TARGET_NUMBER = tmpSrc;
        EventBus.getDefault().post(new MessageModel(MessageModel.CALL,protocol,ctx.channel()));
    }
}
