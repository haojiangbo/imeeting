package com.haojiangbo.http;

import lombok.Data;

import java.io.Serializable;
/**
* @Title: HttpRequest
* @Package com.haojiangbo.http
* @Description: http请求类
* @author Administrator
* @date 2020/4/14
* @version V1.0
*/
@Data
public class HttpRequest implements Serializable {
    private String method;// 请求方法
    private String protocol;// 协议版本
    private String requestURI;//请求的URI地址 在HTTP请求的第一行的请求方法后面
    private String host;//请求的主机信息
    private String Connection;//Http请求连接状态信息 对应HTTP请求中的Connection
    private String agent;// 代理，用来标识代理的浏览器信息 ,对应HTTP请求中的User-Agent:
    private String language;//对应Accept-Language
    private String encoding;//请求的编码方式 对应HTTP请求中的Accept-Encoding
    private String charset;//请求的字符编码 对应HTTP请求中的Accept-Charset
    private String accept;// 对应HTTP请求中的Accept;
}
