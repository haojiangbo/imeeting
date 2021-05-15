package com.haojiangbo.hander;

import com.haojiangbo.config.SessionChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.container.BridgeClientContainer;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.utils.SessionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.ConvolveOp;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 　　* @author 郝江波
 * 　　* @date 2020/4/17 10:47
 *
 */
@Slf4j
public class ClientHander extends ChannelInboundHandlerAdapter {

    private static Bootstrap bootstrap;
    private static BridgeClientContainer bridgeClientContainer;

    public ClientHander(Bootstrap btp, BridgeClientContainer bc) {
        bootstrap = btp;
        bridgeClientContainer = bc;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomProtocol message = (CustomProtocol) msg;
        //log.info("接收到的消息 = {}",message.getContent().copy().toString(Charset.forName("UTF-8")));
        log.info("接收到的消息 = {} byte", message.getContent().readableBytes());
        switch (message.getMeesgeType()) {
            case ConstantValue.PING:
                pingHander(ctx, message);
                break;
            case ConstantValue.DATA:
                dataHander(ctx, message);
                break;
            case ConstantValue.CONCAT:
                createConnect(ctx, message);
                break;
            case ConstantValue.CLOSE:
                Channel target = SessionChannelMapping.SESSION_CHANNEL_MAPPING.get(message.getSessionId());
                log.info("收到服务端关闭事件，关闭一个链接 {}", target);
                if (null != target) {
                    target.close();
                }
                break;
            default:
                break;
        }
    }

    private void dataHander(ChannelHandlerContext ctx, CustomProtocol message) {
        String sessionId = message.getSessionId();
        Channel target = SessionChannelMapping.SESSION_CHANNEL_MAPPING.get(sessionId);
        if (target == null || !target.isActive()) {
            createConnect(ctx, message);
        } else {
            target.writeAndFlush(message.getContent());
        }
        ReferenceCountUtil.release(message);
    }

    private void createConnect(ChannelHandlerContext ctx, CustomProtocol message) {
        // 连接服务端
        // 2020/08/29日改
        //ctx.channel().config().setOption(ChannelOption.AUTO_READ,false);

        SessionUtils.SessionModel model = SessionUtils.parserSessionId(message.getSessionId());
        bootstrap.connect(model.getHost(), model.getPort())
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        //此处的sessionId 是为了方便转发数据的时候获取sessionId
                        future.channel().attr(NettyProxyMappingConstant.SESSION).set(message.getSessionId());
                        //关联当前管道
                        future.channel().attr(NettyProxyMappingConstant.MAPPING).set(ctx.channel());
                        //下次服务端再有数据的时候，不需要重新连接
                        SessionChannelMapping.SESSION_CHANNEL_MAPPING.put(message.getSessionId(), future.channel());
                        if (message.getMeesgeType() != ConstantValue.CONCAT) {
                            //向服务发送数据
                            future.channel().writeAndFlush(message.getContent());
                        }else {
                            ReferenceCountUtil.release(message);
                        }
                    } else {
                        ByteBuf errorMsg =  Unpooled.wrappedBuffer("CONCAT_ERROR".getBytes());
                        // 如果连接本地服务失败，则发送个失败的消息
                        ctx.channel().writeAndFlush(new CustomProtocol(
                                ConstantValue.CONCAT_ERROR,
                                message.getSessionId().getBytes().length,
                                message.getSessionId(),
                                errorMsg.readableBytes(),
                                errorMsg
                        ));
                        ReferenceCountUtil.release(message);
                    }
                });
    }


    private void pingHander(ChannelHandlerContext ctx, CustomProtocol message) {
        //log.info("收到服务器的心跳消息  clientId = {}",SessionUtils.parserSessionId(message.getSessionId()).getClientId());
        String meesgae = message.getContent().toString(CharsetUtil.UTF_8);
        if (meesgae.equals(ConstantValue.CLIENTID_ERROR)
                || meesgae.equals(ConstantValue.REPEATED_ERROR)) {
            log.error("服务器返回错误消息 {} ", meesgae);
            BridgeClientContainer.IS_RESTART = false;
            bridgeClientContainer.stop();
            return;
        }
        //释放消息，防止内存泄漏
        ReferenceCountUtil.release(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        bridgeClientContainer.restart();
    }
}
