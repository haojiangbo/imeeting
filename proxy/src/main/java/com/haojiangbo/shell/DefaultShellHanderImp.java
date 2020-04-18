package com.haojiangbo.shell;

import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.hander.EventClientHander;
import com.haojiangbo.model.CustomProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.UUID;

/**
  *
  * 默认的shell事件处理器
 　　* @author 郝江波
 　　* @date 2020/4/18 11:18
 　　*/
public class DefaultShellHanderImp extends  ShellHanderAbstract{
    String sessionId = "134";
    @Override
    protected String flush() {
        if(EventClientHander.channel != null && EventClientHander.channel.isActive()){
            ByteBuf send = Unpooled.wrappedBuffer(ShellHanderAbstract.FLUSH.getBytes());
            EventClientHander.channel.writeAndFlush(new CustomProtocol(
                    ConstantValue.DATA,
                    sessionId.getBytes().length ,
                    sessionId, send.readableBytes(),
                    send));
            return ShellHanderAbstract.FLUSH + " 发送成功";
        }else{
            return ShellHanderAbstract.FLUSH + " 发送失败";
        }
    }

     @Override
     protected String exit() {
         return ShellHanderAbstract.EXIT;
     }
 }
