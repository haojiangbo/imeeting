package com.haojiangbo.start;

import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.container.BridgeEngineContainner;
import com.haojiangbo.container.EventEngineContainner;
import com.haojiangbo.container.ProxyEngineContainer;
import com.haojiangbo.container.SentryEngineContainer;
import com.haojiangbo.event.AbstractEventAccepter;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.model.EventMessage;
import lombok.extern.slf4j.Slf4j;

/**
 　　* @author 郝江波
 　　* @date 2020/4/15 15:53
 　　*/
@Slf4j
public class StartProxyServerApp extends AbstractEventAccepter {
    private  static  Container[]  containers = new Container[]{
            new ServerConfig(),
            new BridgeEngineContainner(),
            new SentryEngineContainer(),
            new ProxyEngineContainer(),
            new EventEngineContainner()
    };

    public static void main(String [] args){
        if(args.length == 0){
           try {
               startContainers();
           }catch (Exception e){
               e.printStackTrace();
               stopContainers();
           }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.error("关闭服务端...");
            stopContainers();
        }));
    }

    private static void startContainers() {
        for(Container item : containers){
            item.start();
        }
    }


    private static void stopContainers() {
        for(Container item : containers){
            item.stop();
        }
    }

    @Override
    public void notify(EventMessage eventMessage) {
        if(eventMessage.getType() == EventMessage.RELOAD_EVENT){
            stopContainers();
            containers = new Container[]{
                    new ServerConfig(),
                    new BridgeEngineContainner(),
                    new SentryEngineContainer(),
                    new ProxyEngineContainer(),
            };
            startContainers();
        }else if(eventMessage.getType() == EventMessage.CLOSE_EVENT){
            stopContainers();
            containers = null;
        }
    }

    @Override
    protected AbstractEventAccepter registInstand() {
        return new StartProxyServerApp();
    }
}
