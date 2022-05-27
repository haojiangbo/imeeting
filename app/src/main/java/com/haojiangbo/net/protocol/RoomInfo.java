package com.haojiangbo.net.protocol;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

public class RoomInfo implements Serializable {
    private String roomnum;
    private String password;

    public String getRoomnum() {
        return roomnum;
    }

    public void setRoomnum(String roomnum) {
        this.roomnum = roomnum;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toJson(){
        return JSON.toJSONString(this);
    }
}
