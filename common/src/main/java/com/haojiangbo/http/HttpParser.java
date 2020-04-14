package com.haojiangbo.http;

import lombok.extern.slf4j.Slf4j;

/**
* @Title: Http1Analysis
* @Package com.haojiangbo.http
* @Description: http1.1解析器
* @author 郝江波
* @date 2020/4/14
* @version V1.0
*/
@Slf4j
public class HttpParser extends AbstractHttpParser{

    @Override
    public void decode(String value) {
        parser(value);
    }

    @Override
    protected void parser(String s) {
       String [] httpContext  =   s.split("\r\n");
       for(String item : httpContext){
           log.info("解析出的数据 > {}",item);
       }
    }


    /**
     * 传入HTTP请求中需要解析的某一句 解析该句，并请放入对应的Request对象中
     *
     * @param s

    private void parser(String s) {
        if (s.startsWith("GET")) {
            String method = "Get";
            request.setMethod(method);

            int index = s.indexOf("HTTP");
            System.out.println("index--->" + index);
            String uri = s.substring(3 + 1, index - 1);// 用index-1可以去掉连接中的空格
            System.out.println("uri--->" + uri);
            request.setRequestURI(uri);

            String protocol = s.substring(index);
            System.out.println("protocol---->" + protocol);
            request.setProtocol(protocol);
        } else if (s.startsWith("POST")) {
            String method = "POST";
            request.setMethod(method);

            int index = s.indexOf("HTTP");
            System.out.println("index--->" + index);
            String uri = s.substring(3 + 1, index - 1);// 用index-1可以去掉连接中的空格
            System.out.println("uri--->" + uri);
            request.setRequestURI(uri);

            String protocol = s.substring(index);
            System.out.println("protocol---->" + protocol);
            request.setProtocol(protocol);

        } else if (s.startsWith("Accept:")) {
            String accept = s.substring("Accept:".length() + 1);
            System.out.println("accept--->" + accept);
            request.setAccept(accept);

        } else if (s.startsWith("User-Agent:")) {
            String agent = s.substring("User-Agent:".length() + 1);
            System.out.println("agent--->" + agent);
            request.setAgent(agent);

        } else if (s.startsWith("Host:")) {
            String host = s.substring("Host:".length() + 1);
            System.out.println("host--->" + host);
            request.setHost(host);

        } else if (s.startsWith("Accept-Language:")) {
            String language = s.substring("Accept-Language:".length() + 1);
            System.out.println("language--->" + language);
            request.setLanguage(language);

        } else if (s.startsWith("Accept-Charset:")) {
            String charset = s.substring("Accept-Charset:".length() + 1);
            System.out.println("charset--->" + charset);
            request.setCharset(charset);
        } else if (s.startsWith("Accept-Encoding:")) {
            String encoding = s.substring("Accept-Encoding:".length() + 1);
            System.out.println("encoding--->" + encoding);
            request.setEncoding(encoding);

        } else if (s.startsWith("Connection:")) {
            String connection = s.substring("Connection:".length() + 1);
            System.out.println("connection--->" + connection);
            request.setConnection(connection);
        }

    }

     */
}
