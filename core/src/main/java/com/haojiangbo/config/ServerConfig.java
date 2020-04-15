package com.haojiangbo.config;

import com.haojiangbo.inteface.Container;
import lombok.Data;

/**
 *
 * 初始化配置类
 *
  * @author 郝江波
  * @date 2020/4/15 16:26
  */
@Data
public class ServerConfig implements Container {

   public static  ServerConfig INSTAND;

   private int proxyPort;
   private int bridgePort;


   public ServerConfig(){
       this.proxyPort = ProxyServerConfig.INSTAND.getIntValue("proxyPort");
       this.bridgePort = ProxyServerConfig.INSTAND.getIntValue("bridgePort");
       INSTAND = this;
   }




   @Override
   public void start() {

   }

   @Override
   public void stop() {

   }
}
