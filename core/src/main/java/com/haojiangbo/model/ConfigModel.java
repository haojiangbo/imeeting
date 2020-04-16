package com.haojiangbo.model;

import lombok.Data;

@Data
public class ConfigModel {
    private String domain;
    private int port;
    private int clientId;
    private String clientUrl;
}
