package com.haojiangbo.model;

import lombok.Data;

@Data
public class ConfigModel {
    private String domain;
    private int port;
    private String clientId;
    private String clientUrl;
}
