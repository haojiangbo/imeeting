package com.haojiangbo.start;

import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.container.*;
import com.haojiangbo.event.EventListener;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.model.EventMessage;
import com.haojiangbo.shell.CmdShellHander;
import lombok.extern.slf4j.Slf4j;

/**
 　　* @author 郝江波
 　　* @date 2020/4/15 15:53
 　　*/
@Slf4j
public class StartProxyServerApp implements EventListener {


    private static StartProxyServerApp instand;


    public StartProxyServerApp(){
        ACCEPTER.add(this);
    }


    private  static  Container[]  containers = getContainers();

    public static void main(String [] args){
        instand = new StartProxyServerApp();
        if(args.length == 0){
            try {
               startContainers();
            }catch (Exception e){
               e.printStackTrace();
               stopContainers();
            }
            shutDownHook();
        }
        // 支持控制台交互
        else if(args[0].equals(ConstantValue.CLI)){
            CmdShellHander.start(new ServerConfig(),new EventClientEngineContainner());
        }
    }

    private static void shutDownHook() {
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
    public void notifyEvent(EventMessage eventMessage) {
        if(eventMessage.getType() == EventMessage.RELOAD_EVENT){
            stopContainers();
            containers = getContainers();
            startContainers();
        }else if(eventMessage.getType() == EventMessage.CLOSE_EVENT){
            stopContainers();
            containers = null;
        }
    }

    private static Container[] getContainers() {
        return new Container[]{
                new ServerConfig(),
                new BridgeEngineContainner(),
                new SentryEngineContainer(),
                new ProxyEngineContainer(),
                new ProxySSLEngineContainer(),
                new EventEngineContainner()
        };
    }

}
