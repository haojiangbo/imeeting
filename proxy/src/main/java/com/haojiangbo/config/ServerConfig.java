package com.haojiangbo.config;

import com.haojiangbo.config.imp.NioLocalFileConfigReadImp;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.model.ConfigModel;
import com.haojiangbo.utils.PathUtils;
import com.haojiangbo.utils.SessionUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 事件监听端口号
     */
   private int eventPort;
    /**
     * 代理端口 反向代理
     */
   private int proxyPort;
    /**
     * 代理端口 https
     */
   private int proxySSLPort;
    /**
     * key的路径
     */
   private String keyStoryPath;
    /**
     * key的密码
     */
   private String keyStoryPassword;
   
    /**
     * 桥接服务器和端口号
     */
   private String bridgeHost;
   private int bridgePort;

    /**
     * 是否开启代理服务器模式 默认为关闭  如果设置为true 请参考文档
     */
   private boolean isProxyModel;
    /**
     * 配置文件缓存
     */
   private List<ConfigModel> configList;
    /**
     *  二级域名和哨兵端口号的映射
     */
   private Map<String,Integer> domainProtMapping = new HashMap<>();


    private List<ConfigModel> initConfigList(){
        ConfigRead configRead =   new NioLocalFileConfigReadImp();
        return   configRead.readLine(PathUtils.getPath(ServerConfig.class));
   }


   @Override
   public void start() {
       setValue();
   }

    private void setValue() {
        this.eventPort = PrppertiesReadUtils.INSTAND.getIntValue("eventPort");
        this.proxyPort = PrppertiesReadUtils.INSTAND.getIntValue("proxyPort");
        this.proxySSLPort = PrppertiesReadUtils.INSTAND.getIntValue("proxySSLPort");
        this.keyStoryPath = PrppertiesReadUtils.INSTAND.getStringValue("keyStoryPath");
        this.keyStoryPassword = PrppertiesReadUtils.INSTAND.getStringValue("keyStoryPassword");
        this.bridgeHost = PrppertiesReadUtils.INSTAND.getStringValue("bridgeHost");
        this.bridgePort = PrppertiesReadUtils.INSTAND.getIntValue("bridgePort");
        this.isProxyModel = PrppertiesReadUtils.INSTAND.getBooleanValue("isProxyModel");
        this.configList = initConfigList();

        if (configList.size() == 0) {
            throw new RuntimeException("配置不能为空");
        }
        for (ConfigModel item : configList) {
            domainProtMapping.put(item.getDomain(), item.getPort());
        }
        // 禁止指令重排序
        INSTAND = this;
    }


    public void restart() {
        setValue();
    }

   @Override
   public void stop() {
       SessionUtils.clearCatch();
       ClientCheckConfig.CLIENT_CHECK_MAP.clear();
   }


   
}
