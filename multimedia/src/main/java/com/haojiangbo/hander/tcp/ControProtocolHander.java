package com.haojiangbo.hander.tcp;

import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.mapping.CallNumberAndChannelMapping;
import com.haojiangbo.protocol.ControlProtocol;
import com.haojiangbo.protocol.MediaDataProtocol;
import com.haojiangbo.protocol.Pod;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 　　* @author 郝江波
 * 　　* @date 2020/11/20 16:13
 *
 */
@Slf4j
public class ControProtocolHander extends ChannelInboundHandlerAdapter {


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ControlProtocol protocol = (ControlProtocol) msg;
        String payLaod = new String(protocol.data);
        switch (protocol.flag) {
            case ControlProtocol.PING:
                log.info("channel={},PING = {}", ctx.channel(), Arrays.toString(protocol.data));
                if (protocol.data.length == 6) {
                    CallNumberAndChannelMapping.NUMBER_CHANNEL_MAPPING.put(new String(protocol.data), ctx.channel());
                }
                break;
            case ControlProtocol.CALL:
                callMessageHander(ctx, protocol, payLaod);
                break;
            case ControlProtocol.CALL_REPLY:
                Pod pod = JSONObject.parseObject(payLaod, Pod.class);
                Channel targetChannel = CallNumberAndChannelMapping.NUMBER_CHANNEL_MAPPING.get(pod.getDst());
                if (null != targetChannel) {
                    if (!targetChannel.isActive()) {
                        CallNumberAndChannelMapping.NUMBER_CHANNEL_MAPPING.remove(pod.getDst());
                        return;
                    }
                    targetChannel.writeAndFlush(protocol);
                }
                break;
            case ControlProtocol.HANG:
                break;
        }
    }

    private void callMessageHander(ChannelHandlerContext ctx, ControlProtocol protocol, String payLaod) {
        Pod pod = JSONObject.parseObject(payLaod, Pod.class);
        Channel targetChannel = CallNumberAndChannelMapping.NUMBER_CHANNEL_MAPPING.get(pod.getDst());
        if (null != targetChannel) {
            if (!targetChannel.isActive()) {
                CallNumberAndChannelMapping.NUMBER_CHANNEL_MAPPING.remove(pod.getDst());
                return;
            }
            targetChannel.writeAndFlush(protocol);
        }
        log.info("channel={},CALL = {}", ctx.channel(), payLaod);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
