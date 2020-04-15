package com.haojiangbo.http;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
* @Title: AbstractHttpAnalysis
* @Package com.haojiangbo.http
* @Description:  http协议解析类
* @author 郝江波
* @date 2020/4/14
* @version V1.0
*/
public abstract class AbstractHttpParser {


    private static final AbstractHttpParser HTTP1_1PARSER = new HttpParser();

    /**
     * 返回一个默认解析器
     * @return
     */
    public static AbstractHttpParser getDefaltHttpParser(){
        return HTTP1_1PARSER;
    }

    public  HttpRequest  decode(ByteBuf byteBuf){
        if(null == byteBuf || byteBuf.writerIndex() == byteBuf.readerIndex()){
             decode("");
             return null;
        }
        return decode(byteBuf.toString(CharsetUtil.UTF_8));
    }

    public abstract HttpRequest  decode(String value);

    protected abstract HttpRequest parser(String s);

}
