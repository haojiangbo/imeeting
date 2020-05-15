package com.haojiangbo.http;

import com.haojiangbo.constant.HttpProtocolConstant;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 郝江波
 * @version V1.0
 * @Title: Http1Analysis
 * @Package com.haojiangbo.http
 * @Description: http1.1解析器
 * @date 2020/4/14
 */
@Slf4j
public class HttpParser extends AbstractHttpParser {

    @Override
    public HttpRequest decode(String value)  {
      return   parser(value);
    }

    @Override
    protected HttpRequest parser(String s) {
        String[] httpContext = s.split("\n");
        HttpRequest request = new HttpRequest();
        for (String item : httpContext) {
            textParsing(item, request);
        }
        log.info("parser obj = \r\n {}", request);
        return request;
    }

    private void textParsing(String item, HttpRequest request) {
        if (item.startsWith(HttpProtocolConstant.POST)
                || item.startsWith(HttpProtocolConstant.GET)
                || item.startsWith(HttpProtocolConstant.PUT)
                || item.startsWith(HttpProtocolConstant.DELETE)
        ) {
            requestUriParser(item, request);
        } else if (item.startsWith(HttpProtocolConstant.HOST)) {
            requestHostParser(item, request);
        }
    }

    private void requestUriParser(String str, HttpRequest request) {
        String[] requestUri = str.split(" ");
        assert requestUri.length == 3;
        request.setMethod(requestUri[0]);
        request.setProtocol(requestUri[2].replace("\r", ""));
        request.setRequestUri(requestUri[1]);
    }

    private void requestHostParser(String str, HttpRequest request) {
        String[] host = str.split(" ");
        assert host.length == 2;
        request.setHost(host[1].replace("\r", ""));
        String [] port =  request.getHost().split(":");
        if(port.length == 2){
            request.setHost(port[0]);
            request.setPort(Integer.valueOf(port[1]));
        }else {
            request.setHost(port[0]);
            request.setPort(80);
        }
    }

}
