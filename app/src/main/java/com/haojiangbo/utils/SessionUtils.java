package com.haojiangbo.utils;

public class SessionUtils {
    public static Model splitSession(String sessionId){
        Model model = new Model();
        model.key = sessionId.substring(0,6);
        model.uid = sessionId.substring(6);
        return model;
    }


    public static class Model{
        public String key;
        public String uid;
    }
}
