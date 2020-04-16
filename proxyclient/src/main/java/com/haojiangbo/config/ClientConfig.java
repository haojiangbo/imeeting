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
public class ClientConfig implements Container {
   public static volatile ClientConfig INSTAND;
   private int clientId;
   private String remoteHost;
   private int remotePort;

   public ClientConfig(){
       this.clientId =  ProxyServerConfig.INSTAND.getIntValue("clientId");
       this.remoteHost = ProxyServerConfig.INSTAND.getStringValue("remoteHost");
       this.remotePort = ProxyServerConfig.INSTAND.getIntValue("remotePort");
       //volatile 关键字 防止指令重排序 顺便刷新本地内存到主存
       INSTAND = this;
   }




   @Override
   public void start() {

   }

   @Override
   public void stop() {

   }
}
