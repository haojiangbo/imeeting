package com.haojiangbo.start;

import com.haojiangbo.config.ProxyServerConfig;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.container.ConnectionBridgeContainner;
import com.haojiangbo.container.ProxyEngineContainer;
import com.haojiangbo.container.SentryEngineContainer;
import com.haojiangbo.inteface.Container;
import lombok.extern.slf4j.Slf4j;

/**
 　　* @author 郝江波
 　　* @date 2020/4/15 15:53
 　　*/
@Slf4j
public class StartProxyServerApp {

    public static void main(String [] args){
        Container[]  containers = new Container[]{
                new ServerConfig(),
                new ProxyEngineContainer(),
                new ConnectionBridgeContainner(),
                new SentryEngineContainer()
        };
        for(Container item : containers){
            item.start();
        }
    }
}
