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
   private String clientId;
   private String remoteHost;
   private int remotePort;

   public ClientConfig(){
       this.clientId =  PrppertiesReadUtils.INSTAND.getStringValue("clientId");
       this.remoteHost = PrppertiesReadUtils.INSTAND.getStringValue("remoteHost");
       this.remotePort = PrppertiesReadUtils.INSTAND.getIntValue("remotePort");
       // 禁止指令重排序
       INSTAND = this;
   }




   @Override
   public void start() {

   }

   @Override
   public void stop() {

   }
}
