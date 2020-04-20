package com.haojiangbo.config;

import com.haojiangbo.model.ConfigModel;

import java.util.List;
/**
* @Title: ConfigRead
* @Package com.haojiangbo.config
* @Description: 配置读取
* @author 好江波
* @date 2020/4/16
* @version V1.0
*/
public interface ConfigRead {
    String CONFIG_FILE_NAME = "config";
    List<ConfigModel> readLine(String path);
}
