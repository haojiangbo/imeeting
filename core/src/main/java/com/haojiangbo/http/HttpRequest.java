package com.haojiangbo.http;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
* @Title: HttpRequest
* @Package com.haojiangbo.http
* @Description: http请求类
* @author 郝江波
* @date 2020/4/14
* @version V1.0
*/
@Data
@Accessors(chain = true)
public class HttpRequest implements Serializable {
    /**
     * 请求方法
     */
    private String method;
    /**
     * 协议版本
     */
    private String protocol;
    /**
     * 请求地址
     */
    private String requestUri;
    /**
     * 请求主机
     */
    private String host;

    /**
     * 端口号
     */
    private int port;

}
