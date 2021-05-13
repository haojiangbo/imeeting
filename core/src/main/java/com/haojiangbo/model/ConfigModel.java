package com.haojiangbo.model;

import lombok.Data;

@Data
public class ConfigModel {
    private String domain;
    private int port;
    private String clientId;
    private String clientUrl;
    // 传输速率 单位为 KB   默认每秒 128 KB
    private int rate;

    @Override
    public String toString() {
        return "[" +
                "访问域名:'" +  domain+".1900th.com"  + '\'' +
                ", 访问端口:" + port +
                ", 代理地址='" + clientUrl + '\'' +
                ']';
    }
}
