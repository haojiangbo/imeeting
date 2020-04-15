package com.haojiangbo.hander;


import com.haojiangbo.config.BrigdeChannelMapping;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 *
 * 桥梁中继hander
  * @author 郝江波
  * @date 2020/4/15 16:36
  */
@Slf4j
public class BrigdeHander extends ChannelInboundHandlerAdapter  {


     @Override
     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CustomProtocol message = (CustomProtocol) msg;
        // 处理管道和消息的映射
        channelMappingHander(ctx, message);
        // byte array 转  byteBuf
        // ByteBuf byteBuf =  Unpooled.wrappedBuffer(message.getContent());
         log.info("比较  a = {} b = {} == {}",message.getClientId(),ConstantValue.PING,message.getClientId() == ConstantValue.PING);
         switch (message.getMeesgeType()){
             case ConstantValue.PING:
                 pingHander(ctx,message);
                 break;
             case ConstantValue.DATA:
                 break;
         }

     }


     private  void  pingHander(ChannelHandlerContext ctx,CustomProtocol message){
         log.info("收到客户端的心跳消息  clientId = {}",message.getClientId());
         ctx.writeAndFlush(message);
     }



      /**
       * 映射管道
       * @param ctx
       * @param message
       */
      private void channelMappingHander(ChannelHandlerContext ctx, CustomProtocol message) {
        if(BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.containsKey(ctx.channel().id().asLongText())){
          BrigdeChannelMapping .CHANNELID_CLINENTID_MAPPING.put(ctx.channel().id().asLongText(),message.getClientId());
        }
        if(BrigdeChannelMapping .CLIENT_ID_MAPPING.containsKey(message.getClientId())){
          BrigdeChannelMapping .CLIENT_ID_MAPPING.put(message.getClientId(),ctx.channel());
        }
      }


      @Override
     public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        removeChannelMapping(ctx);
        super.channelInactive(ctx);
     }


      /**
       * 删除对应的映射
       * @param ctx
       */
      private void removeChannelMapping(ChannelHandlerContext ctx) {
        if(BrigdeChannelMapping.CHANNELID_CLINENTID_MAPPING.containsKey(ctx.channel().id().asLongText())){
           int key =   BrigdeChannelMapping .CHANNELID_CLINENTID_MAPPING.get(ctx.channel().id().asLongText());
           BrigdeChannelMapping .CLIENT_ID_MAPPING.remove(key);
           BrigdeChannelMapping .CHANNELID_CLINENTID_MAPPING.remove(ctx.channel().id().asLongText());
        }
      }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
          super.exceptionCaught(ctx, cause);
    }
}
