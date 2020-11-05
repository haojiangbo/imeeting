package com.haojiangbo.datamodel;
 /**
　　* @author 郝江波
　　* @date 2020/10/26 14:22
　　*/
public class HDatabaseTableModel {
     /**
      *  数据库名称 默认
      */
    public static final String  DEFULT_DATABASE_NAME = "HDATABASE";
    public static final String  PRIMARY  = "PRIMARY";
     /**
      * 头部大小
      */
    int headerSize;
     /**
      * 头部信息
      * 包含表结构
      * 使用json存储
      */
    String headerInfo;
}
