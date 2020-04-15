package com.haojiangbo.start;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.container.BridgeClientContainer;
import com.haojiangbo.inteface.Container;

public class ClientAppStart {
    public static void main(String [] ages){
        Container[] containers = new Container[]{new ClientConfig(),new BridgeClientContainer()};
        for(Container container : containers){
            container.start();
        }
    }
}
