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

import java.nio.charset.Charset;

/**
 * 桥梁中继hander
 *
 * @author haojiangbo
 * @date 2020/4/15 16:36
 */
@Slf4j
public class BrigdeHander extends ChannelInboundHandlerAdapter {

    private volatile  long  flag = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomProtocol message = (CustomProtocol) msg;
        log.info("BrigdeHander 发送数据 == {} byte",message.getContent().readableBytes());
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
                forWardHander(ctx, message,ConstantValue.DATA);
                break;
            case ConstantValue.CONCAT:
                //处理本地哨兵数据的转发
                forWardHander(ctx, message,ConstantValue.CONCAT);
                break;
            case ConstantValue.CLOSE:
                //处理本地哨兵数据的转发
                forWardHander(ctx, message,ConstantValue.CLOSE);
                break;
            case ConstantValue.CONCAT_RPLAY:
                //处理本地哨兵数据的转发
                dataHander(ctx, message);
                break;
            default:
                break;
        }
    }


    private void forWardHander(ChannelHandlerContext ctx, CustomProtocol message,int type) {
        //log.info("FFF1 收到哨兵端的数据 {} byte channel = {} ,消息类型 = {}", message.getContent().readableBytes(),ctx.channel(),type);
        String clientId = SessionUtils.parserSessionId(message.getSessionId()).getClientId();
        //log.info("FFF2 准备转发至客户端解析 clientId = {},sesisonId = {}",clientId,message.getSessionId());
        Channel target = BrigdeChannelMapping.CLIENT_ID_MAPPING.get(clientId);
        if (null == target || !target.isActive()) {
            log.info("FFF3ERROR 客户端 {} 已失效 强制关闭 桥接 -> 哨兵客户端 的链接 ",clientId);
            ctx.close();
            ReferenceCountUtil.release(message);
            return;
        }

        //绑定这个会话  和 哨兵的 channel 连接
        SessionChannelMapping.SESSION_CHANNEL_MAPPING.put(message.getSessionId(), ctx.channel());
        flag = System.currentTimeMillis();
        //向客户端发送消息
        target.writeAndFlush(message.setMeesgeType(type)).addListener((ChannelFutureListener) future -> {
           //log.info("FFF3 转发至客户端发送成功 clientId = {},sesisonId = {} ",clientId,message.getSessionId());
        });


    }

    private void dataHander(ChannelHandlerContext ctx, CustomProtocol message) {
        //log.info("RRR1 收到客户端的数据 {} byte", message.getContent().readableBytes());
        Channel target = SessionChannelMapping.SESSION_CHANNEL_MAPPING.get(message.getSessionId());
        if (null == target || !target.isActive()) {
            SessionChannelMapping.SESSION_CHANNEL_MAPPING.remove(message.getSessionId());
            // 此处关闭的是 客户端 到 服务端的链接 是一个bug
            // ctx.close();
            //log.info("RRR2 使用sessionId {} get 哨兵 channel已关闭或为空",message.getSessionId());
            ReferenceCountUtil.release(message);
        } else {
            // 向哨兵客户端发送数据
            target.writeAndFlush(message).addListener((ChannelFutureListener) channelFuture -> {
                if(channelFuture.isSuccess()){
                    //log.info("RRR2 向哨兵端发送 {}", channelFuture.isSuccess());
                }
            });
        }
    }

    private void pingHander(ChannelHandlerContext ctx, CustomProtocol message, boolean isclose) {
        String clientId = SessionUtils.parserSessionId(message.getSessionId()).getClientId();
        log.info("收到客户端的心跳消息  clientId = {}", clientId);
        ConfigModel configMode = ClientCheckConfig.CLIENT_CHECK_MAP.get(clientId);
        if(null != configMode){
            ByteBuf byteBuf = Unpooled.wrappedBuffer(configMode.toString().getBytes());
            message.setContentLength(byteBuf.readableBytes());
            message.setContent(byteBuf);
        }
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
        String clientId = SessionUtils.parserSessionId(message.getSessionId()).getClientId();
        if (!ClientCheckConfig.CLIENT_CHECK_MAP.containsKey(clientId)) {
            log.error("clientid = {}  不合法",clientId);
            createError(ctx, message, ConstantValue.CLIENTID_ERROR);
            return;
        }
        if (!BrigdeChannelMapping.CLIENT_ID_MAPPING.containsKey(clientId)) {
            BrigdeChannelMapping.CLIENT_ID_MAPPING.put(clientId, ctx.channel());
            // 返回心跳响应
        } else {
            boolean b = BrigdeChannelMapping.CLIENT_ID_MAPPING.get(clientId).equals(ctx.channel());
            if (!b) {
                createError(ctx, message, ConstantValue.REPEATED_ERROR);
                BrigdeChannelMapping.CLIENT_ID_MAPPING.remove(clientId);
                return;
            }
        }
        if (!BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.containsKey(ctx.channel().id().asLongText())) {
            BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.put(ctx.channel().id().asLongText(), clientId);
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
                .setContent(error), false);
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
            //String key = BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.get(ctx.channel().id().asLongText());
            //BrigdeChannelMapping.CLIENT_ID_MAPPING.remove(key);
            BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.remove(ctx.channel().id().asLongText());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }
}
