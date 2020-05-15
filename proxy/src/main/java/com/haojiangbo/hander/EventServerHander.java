package com.haojiangbo.hander;

import com.haojiangbo.event.EventListener;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.model.EventMessage;
import com.haojiangbo.shell.AbstractShellHander;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
  * 事件处理器
 　　* @author jb
 　　* @date 2020/4/18 10:54
 　　*/
 @Slf4j
public class EventServerHander  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            CustomProtocol customProtocol = (CustomProtocol) msg;
            String event = customProtocol.getContent().toString(CharsetUtil.UTF_8);
            EventMessage eventMessage = new EventMessage();
            eventMessage.setType(EventMessage.RELOAD_EVENT);
            eventMessage.setMessage(event);
            if(event.equals(AbstractShellHander.FLUSH)){
               for(EventListener listener :  EventListener.ACCEPTER){
                   listener.notifyEvent(eventMessage);
               }
            }
            log.info("收到event事件 {}", event);
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
