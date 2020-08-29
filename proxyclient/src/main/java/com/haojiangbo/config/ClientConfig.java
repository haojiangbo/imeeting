package com.haojiangbo.config;

import com.haojiangbo.inteface.Container;
import com.haojiangbo.utils.PathUtils;
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
       PrppertiesReadUtils prppertiesReadUtils =   new PrppertiesReadUtils()
               .initConfig(PathUtils.getPath(ClientConfig.class)+PrppertiesReadUtils.DEFAULT_CONF,false);
       this.clientId =  prppertiesReadUtils.getStringValue("clientId");
       this.remoteHost = prppertiesReadUtils.getStringValue("remoteHost");
       this.remotePort = prppertiesReadUtils.getIntValue("remotePort");
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
