package com.haojiangbo.thread;

import com.haojiangbo.hander.tcp.ControProtocolHander;
import com.haojiangbo.mapping.CallNumberAndChannelMapping;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class CheckChannelIsOnline implements Runnable{


    public CheckChannelIsOnline(){
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(10000);
                log.info("检查离线设备...");
                Set<ChannelHandlerContext> keys =  CallNumberAndChannelMapping.CHECK_IS_ONLINE_MAPPING.keySet();
                for(ChannelHandlerContext context : keys){
                    Long time =  CallNumberAndChannelMapping.CHECK_IS_ONLINE_MAPPING.get(context);
                    if(null != time && Math.abs(time - System.currentTimeMillis()) > 30000){
                        // 删除离线设备
                        ControProtocolHander.removeDeadLineChannel(context);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
