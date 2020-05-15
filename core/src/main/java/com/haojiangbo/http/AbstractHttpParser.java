package com.haojiangbo.http;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * @author 郝江波
 * @version V1.0
 * @Title: AbstractHttpAnalysis
 * @Package com.haojiangbo.http
 * @Description: http协议解析类
 * @date 2020/4/14
 */
public abstract class AbstractHttpParser {


    private static final AbstractHttpParser HTTP1_1PARSER = new HttpParser();

    /**
     * 返回一个默认解析器
     *
     * @return
     */
    public static AbstractHttpParser getDefaltHttpParser() {
        return HTTP1_1PARSER;
    }

    public HttpRequest decode(ByteBuf byteBuf) {
        if (null == byteBuf || byteBuf.writerIndex() == byteBuf.readerIndex()) {
            decode("");
            return null;
        }
        return decode(byteBuf.toString(CharsetUtil.UTF_8));
    }

    /**
     * decode
     *
     * @param value
     * @return
     */
    public abstract HttpRequest decode(String value);

    /**
     * parser
     *
     * @param s
     * @return
     */
    protected abstract HttpRequest parser(String s);

}
