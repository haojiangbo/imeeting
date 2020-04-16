package com.haojiangbo.hander;

import com.haojiangbo.config.BrigdeChannelMapping;
import com.haojiangbo.config.SessionChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.constant.NettyProxyMappingConstant;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.utils.SessionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
 /**
  * 哨兵数据处理器
 　　* @author 郝江波
 　　* @date 2020/4/16 13:42
 　　*/
public class SentryHander extends ChannelInboundHandlerAdapter {
    private String clientId = null;
    public SentryHander(String clientId){
        this.clientId = clientId;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String sessionId = SessionUtils.genSessionId(this.clientId, "127.0.0.1:80");

        //绑定sessionId
        ctx.channel().attr(NettyProxyMappingConstant.SESSION)
                .set(sessionId);

        //添加映射关系
        SessionChannelMapping.SESSION_CHANNEL_MAPPING.put(sessionId, ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SessionChannelMapping.SESSION_CHANNEL_MAPPING.remove(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf btf = (ByteBuf) msg;

        //找出客户端和服务端的通道
        SessionUtils.SessionModel model =  SessionUtils
                .parserSessionId(ctx.channel().attr(NettyProxyMappingConstant.SESSION).get());
        Channel target =  BrigdeChannelMapping.CLIENT_ID_MAPPING.get(model.getClientId());
        byte[] result = ByteBufUtil.getBytes(btf);


        //通过中间通道 向客户端链接发送消息
        if(target != null && target.isActive()){
            //向客户端发送消息
            target.writeAndFlush(new CustomProtocol(
                    ConstantValue.DATA,
                    Integer.valueOf(model.getClientId()),
                    model.getSessionId().getBytes().length,
                    model.getSessionId(),
                    result.length,
                    result
            ));
            ReferenceCountUtil.release(btf);
        }else{
            ctx.close();
        }
    }
}
