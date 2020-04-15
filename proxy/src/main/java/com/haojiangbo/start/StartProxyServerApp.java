package com.haojiangbo.start;

import com.haojiangbo.container.ProxyEngineContainer;
import com.haojiangbo.inteface.Container;

/**
 　　* @author 郝江波
 　　* @date 2020/4/15 15:53
 　　*/
public class StartProxyServerApp {

    public static void main(String [] args){
        Container[]  containers = new Container[]{new ProxyEngineContainer()};
        for(Container item : containers){
            item.start();
        }
    }
}
