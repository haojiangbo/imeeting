package com.haojiangbo.start;

import com.haojiangbo.config.ClientConfig;
import com.haojiangbo.container.BridgeClientContainer;
import com.haojiangbo.inteface.Container;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientAppStart {
    private static Container[] containers = new Container[]{new ClientConfig(),new BridgeClientContainer()};

    public static void main(String [] ages){
        startContainers();
        shutdownHook();
    }

    private static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.error("关闭客户端...");
            BridgeClientContainer.IS_RESTART = false;
            stopContainers();
        }));
    }

    private static void startContainers() {
        for(Container container : containers){
            container.start();
        }
    }


    private static void stopContainers() {
        for(Container container : containers){
            container.stop();
        }
    }

}
