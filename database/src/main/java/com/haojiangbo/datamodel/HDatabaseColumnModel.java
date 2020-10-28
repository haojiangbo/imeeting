package com.haojiangbo.datamodel;

import com.alibaba.fastjson.annotation.JSONField;

import java.nio.ByteBuffer;

/**
 * 列模型
 * 采用最简单的方式
 * 不考虑，磁盘块大小
 * 不考虑内存对齐
 * 占用多余空间较大
 * @author 郝江波
 * @date 2020/10/26 14:15
 *
 */
public class HDatabaseColumnModel {
    String name;
    byte type;
    int size;
    @JSONField(serialize = false)
    byte [] data;

    //解析后的value
    Object value;


    /**
     * 转换成byte数组
     * @return
     */
    public  byte[] toByteArray(){
        ByteBuffer bytes = ByteBuffer.allocate(1 + 4 + this.data.length);
        bytes.put(this.type);
        bytes.putInt(this.data.length);
        bytes.put(this.data);
        return bytes.array();
    }


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
