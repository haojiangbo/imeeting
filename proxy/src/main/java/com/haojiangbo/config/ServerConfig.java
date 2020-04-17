package com.haojiangbo.config;

import com.haojiangbo.config.imp.NioLocalFileConfigReadImp;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.model.ConfigModel;
import com.haojiangbo.utils.PathUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * 初始化配置类
 *
  * @author 郝江波
  * @date 2020/4/15 16:26
  */
@Data
@Slf4j
public class ServerConfig implements Container {

   public static volatile   ServerConfig INSTAND;

   private String bridgeHost;
   private int proxyPort;
   private int bridgePort;
   private List<ConfigModel> configList;


   public ServerConfig(){
       this.proxyPort = ProxyServerConfig.INSTAND.getIntValue("proxyPort");
       this.bridgeHost = ProxyServerConfig.INSTAND.getStringValue("bridgeHost");
       this.bridgePort = ProxyServerConfig.INSTAND.getIntValue("bridgePort");
       this.configList = initConfigList();
       if (configList.size() == 0) {
           throw  new RuntimeException("配置不能为空");
       }
       // 禁止指令重排序
       INSTAND = this;
   }


    private List<ConfigModel> initConfigList(){
        ConfigRead configRead =   new NioLocalFileConfigReadImp();
        return   configRead.readLine(PathUtils.getPath(ServerConfig.class));
   }


   @Override
   public void start() {

   }

   @Override
   public void stop() {

   }
}
