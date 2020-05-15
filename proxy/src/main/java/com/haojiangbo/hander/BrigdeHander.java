package com.haojiangbo.hander;


import com.haojiangbo.config.BrigdeChannelMapping;
import com.haojiangbo.config.ClientCheckConfig;
import com.haojiangbo.config.SessionChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.model.ConfigModel;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.utils.SessionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 桥梁中继hander
 *
 * @author haojiangbo
 * @date 2020/4/15 16:36
 */
@Slf4j
public class BrigdeHander extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomProtocol message = (CustomProtocol) msg;

        switch (message.getMeesgeType()) {
            case ConstantValue.PING:
                // 处理管道和消息的映射
                channelMappingHander(ctx, message);
                break;
            case ConstantValue.DATA:
                // 处理管道和消息的映射
                dataHander(ctx, message);
                break;
            case ConstantValue.FORWARD:
                //处理本地哨兵数据的转发
                forWardHander(ctx, message);
                break;
            default:
                break;
        }
    }


    private void forWardHander(ChannelHandlerContext ctx, CustomProtocol message) {
        log.info("收到哨兵端的消息 {} byte address  = {}", message.getContent().readableBytes(), message.hashCode());
        //ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);
        Channel target = BrigdeChannelMapping.CLIENT_ID_MAPPING.get(SessionUtils.parserSessionId(message.getSessionId()).getClientId());
        if (null == target || !target.isActive()) {
            ctx.close();
            ReferenceCountUtil.release(message);
            return;
        }
        //向客户端发送消息
        target.writeAndFlush(message.setMeesgeType(ConstantValue.DATA)).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                //ctx.channel().config().setOption(ChannelOption.AUTO_READ,true);
            }
        });
        SessionChannelMapping.SESSION_CHANNEL_MAPPING.put(message.getSessionId(), ctx.channel());

    }

    private void dataHander(ChannelHandlerContext ctx, CustomProtocol message) {
        log.info("收到客户端的消息 {} byte", message.getContent().readableBytes());
        Channel target = SessionChannelMapping.SESSION_CHANNEL_MAPPING.get(message.getSessionId());
        if (null == target || !target.isActive()) {
            SessionChannelMapping.SESSION_CHANNEL_MAPPING.remove(message.getSessionId());
            ctx.close();
            ReferenceCountUtil.release(message);
        } else {
            target.writeAndFlush(message);
        }
    }

    private void pingHander(ChannelHandlerContext ctx, CustomProtocol message, boolean isclose) {
        String clientId = SessionUtils.parserSessionId(message.getSessionId()).getClientId();
        log.info("收到客户端的心跳消息  clientId = {}", clientId);
        ConfigModel configMode = ClientCheckConfig.CLIENT_CHECK_MAP.get(clientId);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(configMode.toString().getBytes());
        message.setContentLength(byteBuf.readableBytes());
        message.setContent(byteBuf);
        ctx.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
            if (isclose) {
                ctx.close();
            }
        });
    }


    /**
     * 映射管道
     *
     * @param ctx
     * @param message
     */
    private void channelMappingHander(ChannelHandlerContext ctx, CustomProtocol message) {
        if (!ClientCheckConfig.CLIENT_CHECK_MAP.containsKey(SessionUtils.parserSessionId(message.getSessionId()).getClientId())) {
            log.error("clientid = {}  不合法", SessionUtils.parserSessionId(message.getSessionId()).getClientId());
            createError(ctx, message, ConstantValue.CLIENTID_ERROR);
            return;
        }
        if (!BrigdeChannelMapping.CLIENT_ID_MAPPING.containsKey(SessionUtils.parserSessionId(message.getSessionId()).getClientId())) {
            BrigdeChannelMapping.CLIENT_ID_MAPPING.put(SessionUtils.parserSessionId(message.getSessionId()).getClientId(), ctx.channel());
            // 返回心跳响应
        } else {
            boolean b = BrigdeChannelMapping.CLIENT_ID_MAPPING.get(SessionUtils.parserSessionId(message.getSessionId()).getClientId()).equals(ctx.channel());
            if (!b) {
                createError(ctx, message, ConstantValue.REPEATED_ERROR);
                return;
            }
        }
        if (!BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.containsKey(ctx.channel().id().asLongText())) {
            BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.put(ctx.channel().id().asLongText(), SessionUtils.parserSessionId(message.getSessionId()).getClientId());
        }
        pingHander(ctx, message, false);
    }

    /**
     * 发送错误消息
     *
     * @param ctx
     * @param message
     * @param errortype
     */
    private void createError(ChannelHandlerContext ctx, CustomProtocol message, String errortype) {
        ByteBuf error = Unpooled.wrappedBuffer(errortype.getBytes());
        pingHander(ctx, message
                .setContentLength(error.readableBytes())
                .setContent(error), true);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        removeChannelMapping(ctx);
        super.channelInactive(ctx);
    }


    /**
     * 删除对应的映射
     *
     * @param ctx
     */
    private void removeChannelMapping(ChannelHandlerContext ctx) {
        if (BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.containsKey(ctx.channel().id().asLongText())) {
            String key = BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.get(ctx.channel().id().asLongText());
            BrigdeChannelMapping.CLIENT_ID_MAPPING.remove(key);
            BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.remove(ctx.channel().id().asLongText());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
