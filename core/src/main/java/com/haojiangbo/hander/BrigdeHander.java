package com.haojiangbo.hander;

import com.haojiangbo.config.BrigdeChannelMapping;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
 /**
  *
  *
  * 桥梁中继hander
 　　* @author 郝江波
 　　* @date 2020/4/15 16:36
 　　*/
public class BrigdeHander extends ChannelInboundHandlerAdapter  {


  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    CustomProtocol message = (CustomProtocol) msg;
    // 处理管道和消息的映射
    channelMappingHander(ctx, message);
    // byte array 转  byteBuf
    ByteBuf byteBuf =  Unpooled.wrappedBuffer(message.getContent());

  }

   /**
    * 映射管道
    * @param ctx
    * @param message
    */
   private void channelMappingHander(ChannelHandlerContext ctx, CustomProtocol message) {
     if(BrigdeChannelMapping .CHANNELID_CLINENTID_MAPPING.containsKey(ctx.channel().id().asLongText())){
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
 }
