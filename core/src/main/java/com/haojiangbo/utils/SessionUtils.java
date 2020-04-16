package com.haojiangbo.utils;

import lombok.Data;

import java.util.UUID;

/**
  * 会话生成工具类
 　　* @author 郝江波
 　　* @date 2020/4/16 9:01
 　　*/
public class SessionUtils {


    public static String  genSessionId(String clientId){
        return clientId+"@"+UUID.randomUUID().toString()+"@";
    }
    public static String  genSessionId(String clientId,String url){
        return genSessionId(clientId)+url;
    }

    public static SessionModel  parserSessionId(String sessionId){
        String[] strings =  sessionId.split("@");
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
        }
        return  sessionModel;
    }

    @Data
    public static class SessionModel{
        private String sessionId;
        private String clientId;
        private String uri;
    }

}
