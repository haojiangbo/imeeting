package com.haojiangbo.config;

import com.haojiangbo.model.ConfigModel;

import java.util.List;

/**
 * @author 好江波
 * @version V1.0
 * @Title: ConfigRead
 * @Package com.haojiangbo.config
 * @Description: 配置读取
 * @date 2020/4/16
 */
public interface ConfigRead {
    String CONFIG_FILE_NAME = "config";

    /**
     * readLine
     *
     * @param path
     * @return
     */
    List<ConfigModel> readLine(String path);
}
