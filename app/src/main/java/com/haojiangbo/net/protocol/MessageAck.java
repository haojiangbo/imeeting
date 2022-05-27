package com.haojiangbo.net.protocol;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class MessageAck implements Serializable {
    private int code ;
    private String msg;
    private Object data;

    public MessageAck(){

    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static MessageAck ok(String msg){
        MessageAck messageAck = new MessageAck();
        messageAck.setCode(0);
        messageAck.setMsg(msg);
        return messageAck;
    }

    public static MessageAck err(String msg){
        MessageAck messageAck = new MessageAck();
        messageAck.setCode(500);
        messageAck.setMsg(msg);
        return messageAck;
    }
    public String toJson(){
        return JSONObject.toJSONString(this);
    }
}
