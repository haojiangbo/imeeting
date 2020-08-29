package com.haojiangbo.config;

import com.haojiangbo.utils.LangUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Properties;

/**
  * 配置类读取工具
　* @author 郝江波
　* @date 2020/4/15 16:09
　*/
@Slf4j
public class PrppertiesReadUtils {
    public static final String DEFAULT_CONF = "config.properties";
    public  static final PrppertiesReadUtils INSTAND = new PrppertiesReadUtils();
    private Properties configuration = new Properties();

    public PrppertiesReadUtils() {
        initConfig(DEFAULT_CONF,true);
    }

    public PrppertiesReadUtils initConfig(String defaultConf,boolean isResource) {
        log.info("config file path = {}",defaultConf);
        InputStream is = null;
        if(isResource){
            is =   PrppertiesReadUtils.class.getClassLoader().getResourceAsStream(defaultConf);
        }else{
            try {
                is =   new FileInputStream(new File(defaultConf));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            configuration.load(is);
            is.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }


    /**
     * 获得配置项。
     *
     * @param key 配置关键字
     *
     * @return 配置项
     */
    public String getStringValue(String key) {
        return configuration.getProperty(key);
    }

    public String getStringValue(String key, String defaultValue) {
        String value = this.getStringValue(key);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public int getIntValue(String key, int defaultValue) {
        return LangUtil.parseInt(configuration.getProperty(key), defaultValue);
    }

    public int getIntValue(String key) {
        return LangUtil.parseInt(configuration.getProperty(key));
    }

    public double getDoubleValue(String key, Double defaultValue) {
        return LangUtil.parseDouble(configuration.getProperty(key), defaultValue);
    }

    public double getDoubleValue(String key) {
        return LangUtil.parseDouble(configuration.getProperty(key));
    }

    public double getLongValue(String key, Long defaultValue) {
        return LangUtil.parseLong(configuration.getProperty(key), defaultValue);
    }

    public double getLongValue(String key) {
        return LangUtil.parseLong(configuration.getProperty(key));
    }

    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return LangUtil.parseBoolean(configuration.getProperty(key), defaultValue);
    }

    public Boolean getBooleanValue(String key) {
        return LangUtil.parseBoolean(configuration.getProperty(key));
    }


}
