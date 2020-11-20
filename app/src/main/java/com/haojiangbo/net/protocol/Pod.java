package com.haojiangbo.net.protocol;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class Pod implements Serializable {
    private String src;
    private String dst;
    private String key;

    public Pod(){ }
    public Pod(String src, String dst, String key) {
        this.src = src;
        this.dst = dst;
        this.key = key;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return  JSONObject.toJSONString(this);
    }
}
