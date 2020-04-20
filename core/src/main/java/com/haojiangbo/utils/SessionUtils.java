package com.haojiangbo.utils;

import lombok.Data;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
  * 会话生成工具类
 　　* @author 郝江波
 　　* @date 2020/4/16 9:01
 　　*/
public class SessionUtils {

    private static  final  String SPLIT = "^";
    /**
     * 本地缓存
     */
    private  static ConcurrentHashMap<String,SessionModel> CATCH = new ConcurrentHashMap<>();

    public static String  genSessionId(String clientId){
        return clientId+SPLIT+UUID.randomUUID().toString()+SPLIT;
    }
    public static String  genSessionId(String clientId,String url){
        return genSessionId(clientId)+url;
    }

    public static SessionModel  parserSessionId(String sessionId){
        if(CATCH.containsKey(sessionId)){
            return CATCH.get(sessionId);
        }
        String[] strings =  sessionId.split("\\"+SPLIT);
        if(strings.length == 1){
            return null;
        }
        SessionModel  sessionModel = new SessionModel();
        if(strings.length == 2){
            sessionModel.setClientId(strings[0]);
            sessionModel.setSessionId(strings[1]);
        }
        if(strings.length == 3){
            sessionModel.setClientId(strings[0]);
            sessionModel.setSessionId(sessionId);
            sessionModel.setUri(strings[2]);
            String [] hp = sessionModel.getUri().split(":");
            sessionModel.setHost(hp[0]);
            sessionModel.setPort(Integer.valueOf(hp[1]));
        }
        CATCH.put(sessionId,sessionModel);
        return  sessionModel;
    }



    public static void clearCatch(){
        CATCH.clear();
    }
    @Data
    public static class SessionModel{
        private String sessionId;
        private String clientId;
        private String uri;
        private String host;
        private int  port;
    }

}
