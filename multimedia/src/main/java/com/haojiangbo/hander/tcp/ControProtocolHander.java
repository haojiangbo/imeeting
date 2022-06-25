package com.haojiangbo.hander.tcp;

import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.mapping.CallNumberAndChannelMapping;
import com.haojiangbo.protocol.*;
import com.haojiangbo.utils.SessionUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 　　* @author 郝江波
 * 　　* @date 2020/11/20 16:13
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
        String session = new String(protocol.session);
        SessionUtils.Model model = SessionUtils.splitSession(session);
        switch (protocol.flag) {
            case ControlProtocol.PING:
                log.info(" tcp session = {}", session, payLaod);
                // 如果session 不存在map里 则不处理下面逻辑
                if(!CallNumberAndChannelMapping.SESSION_CHANNEL_MAPPING.containsKey(session)){
                    return;
                }
                // 活跃用户
                CallNumberAndChannelMapping.CHECK_IS_ONLINE_MAPPING.put(ctx,System.currentTimeMillis());
                addActiviteChannel(ctx, session, model);
                break;
            case ControlProtocol.CREATE: {
                RoomInfo roomInfo = JSONObject.parseObject(payLaod, RoomInfo.class);
                MessageAck messageAck = MessageAck.ok("创建成功");
                RoomInfo tmp = CallNumberAndChannelMapping.ROOM_MAPPING.get(roomInfo.getRoomnum());
                if (null != tmp && !tmp.getChannelId().equals(ctx.channel().id().asLongText())) {
                    messageAck = MessageAck.err("该房间已存在，无法继续创建");
                } else {
                    roomInfo.setChannelId(ctx.channel().id().asLongText());
                    CallNumberAndChannelMapping.ROOM_MAPPING.put(roomInfo.getRoomnum(), roomInfo);
                    CallNumberAndChannelMapping.SESSION_CHANNEL_MAPPING.put(session, ctx.channel());
                }
                // 添加channcel
                addActiviteChannel(ctx, session, model);
                Map<String, Channel> uidsMap = CallNumberAndChannelMapping.ROOM_CHANNEL_MAPPING.get(model.key);
                messageAck.setData(uidsMap.keySet().stream().map(item -> model.key + item).collect(Collectors.toList()));
                ctx.channel().writeAndFlush(new ControlProtocol(ControlProtocol.CREATE_REPLAY, messageAck.toJson().getBytes()));
                break;
            }
            case ControlProtocol.JOIN:
                RoomInfo joinRoom = JSONObject.parseObject(payLaod, RoomInfo.class);
                RoomInfo roomMap = CallNumberAndChannelMapping.ROOM_MAPPING.get(joinRoom.getRoomnum());
                if (null == roomMap) {
                    ctx.channel().writeAndFlush(new ControlProtocol(ControlProtocol.JOIN_REPLAY, MessageAck.err("房间不存在").toJson().getBytes()));
                    return;
                }
                if (null != roomMap && !roomMap.getPassword().equals(joinRoom.getPassword())) {
                    ctx.channel().writeAndFlush(new ControlProtocol(ControlProtocol.JOIN_REPLAY, MessageAck.err("密码错误").toJson().getBytes()));
                    return;
                }
                CallNumberAndChannelMapping.SESSION_CHANNEL_MAPPING.put(session,ctx.channel());
                // 添加channcel
                addActiviteChannel(ctx, session, model);
                Map<String, Channel> uidsMap = CallNumberAndChannelMapping.ROOM_CHANNEL_MAPPING.get(model.key);
                MessageAck ack =  MessageAck.ok("加入成功");
                ack.setData(uidsMap.keySet().stream().map(item -> model.key+item).collect(Collectors.toList()));

                // 挨个发送加入信息
                uidsMap.keySet().stream().forEach(key -> {
                    Channel tmpChanael =   uidsMap.get(key);
                    if(null != tmpChanael && tmpChanael.isActive()){
                        tmpChanael.writeAndFlush(new ControlProtocol(ControlProtocol.JOIN_REPLAY, ack.toJson().getBytes()));
                    }
                });
               // ctx.channel().writeAndFlush(new ControlProtocol(ControlProtocol.JOIN_REPLAY, ack.toJson().getBytes()));
        }
    }

    private void addActiviteChannel(ChannelHandlerContext ctx, String session, SessionUtils.Model model) {
        Map<String, Channel> channelMap = CallNumberAndChannelMapping.ROOM_CHANNEL_MAPPING.get(model.key);
        if (null == channelMap) {
            channelMap = new ConcurrentHashMap<>();
            CallNumberAndChannelMapping.ROOM_CHANNEL_MAPPING.put(model.key, channelMap);
        }
        channelMap.put(model.uid, ctx.channel());
        // 管道 与 会话 映射
        CallNumberAndChannelMapping.CHANNEL_SESSION_MAPPING.put(ctx.channel(), session);
    }

    private void forwardMessage(ChannelHandlerContext ctx, ControlProtocol protocol, String payLaod) {
       /* Pod pod = JSONObject.parseObject(payLaod, Pod.class);
        Channel targetChannel = CallNumberAndChannelMapping.NUMBER_CHANNEL_MAPPING.get(pod.getDst());
        if (null != targetChannel) {
            if (!targetChannel.isActive()) {
                CallNumberAndChannelMapping.NUMBER_CHANNEL_MAPPING.remove(pod.getDst());
                return;
            }
            targetChannel.writeAndFlush(protocol);
        }*/
        log.info("channel={},payload = {}", ctx.channel(), payLaod);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // 删除离线channel
        removeDeadLineChannel(ctx);
    }

    /**
     * 删除离线channel
     * @param ctx
     */
    public static void removeDeadLineChannel(ChannelHandlerContext ctx) {
        String session = CallNumberAndChannelMapping.CHANNEL_SESSION_MAPPING.get(ctx.channel());
        if (StringUtils.isEmpty(session)) {
            return;
        }
        SessionUtils.Model model = SessionUtils.splitSession(session);

        // 删除房主信息
        RoomInfo tmp = CallNumberAndChannelMapping.ROOM_MAPPING.get(model.key);
        if (null != tmp && tmp.getChannelId().equals(ctx.channel().id().asLongText())) {
            CallNumberAndChannelMapping.ROOM_MAPPING.remove(model.key);
        }
        // 删除tcp用户
        Map<String, Channel>  tcpMap = CallNumberAndChannelMapping.ROOM_CHANNEL_MAPPING.get(model.key);
        if (null != tcpMap) {
            tcpMap.remove(model.uid);
            log.info("tcp go back roomid = {}  uid = {} ", model.key, model.uid);

            MessageAck ack =  MessageAck.ok("退出事件");
            ack.setData(session);
            // 挨个发送加入信息
            tcpMap.keySet().stream().forEach(key -> {
                Channel tmpChanael =   tcpMap.get(key);
                if(null != tmpChanael && tmpChanael.isActive()){
                    tmpChanael.writeAndFlush(new ControlProtocol(ControlProtocol.CLOSE, ack.toJson().getBytes()));
                }
            });
        }
        // 删除udp用户
        Map udpMap = CallNumberAndChannelMapping.UDP_ROOM_CHANNEL_MAPPING.get(model.key);
        if (null != tcpMap) {
            udpMap.remove(model.uid);
            log.info("udp go back roomid = {}  uid = {} ", model.key, model.uid);
        }
        CallNumberAndChannelMapping.CHECK_IS_ONLINE_MAPPING.remove(ctx);
        ctx.channel().close();
    }
}
