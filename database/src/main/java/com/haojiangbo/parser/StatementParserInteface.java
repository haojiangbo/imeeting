package com.haojiangbo.parser;
/**
* @Title: StatementParserInteface
* @Package com.haojiangbo.parser
* @Description: 通用接口类
* @author 郝江波
* @date 2020/10/28
* @version V1.0
*/
public interface StatementParserInteface<T,R>{
      R  parser(T statement);
}
