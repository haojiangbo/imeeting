package com.haojiangbo.net.config;

import io.netty.util.AttributeKey;

public class NettyKeyConfig {
     private static String HOST = "10.10.10.218";
    // private static String HOST = "47.104.105.133";
    // 192.168.43.184 yishoutong.hbweiyinqing.cn
    private static int PORT = 10086;
    public static final AttributeKey<String> SESSION_KEY =  AttributeKey.newInstance("SESSION_KEY");

    public static String getHOST() {
        return HOST;
    }

    public static void setHOST(String HOST) {
        NettyKeyConfig.HOST = HOST;
    }

    public static int getPORT() {
        return PORT;
    }

    public static void setPORT(int PORT) {
        NettyKeyConfig.PORT = PORT;
    }
}
