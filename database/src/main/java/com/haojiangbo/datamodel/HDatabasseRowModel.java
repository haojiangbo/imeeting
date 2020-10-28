package com.haojiangbo.datamodel;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 行模型
 * 使用4个字节标记每一行占用空间大小
 */
public class HDatabasseRowModel {
    /**
     * 可用
     */
    public static final byte ENABLE = 1;
    /**
     * 被删除
     */
    public static final byte DELETE = 2;
    /**
     * 标记位 2 是删除 1 是可用
     */
    private byte flag;
    /**
     * 文件偏移量
     */
    private long offset;
    /**
     * 版本号
     */
    private long version;
    /**
     * 占用字节数
     */
    private int size;


    Map<String, HDatabaseColumnModel> data;



    public byte[] toByteArray(){
        if(null == this.data || this.data.size() == 0){
            return  null;
        }
        List<HDatabaseColumnModel> columnModels = new LinkedList<>();
        for(Map.Entry<String, HDatabaseColumnModel> entry : this.data.entrySet()){
            columnModels.add(entry.getValue());
        }
        return toByteArray(columnModels,this.offset,this.flag,this.version);
    }

    public byte[] toByteArray(List<HDatabaseColumnModel> columnModels, long offset, byte flag, long version) {
        int columnSize = 0;
        for (HDatabaseColumnModel model : columnModels) {
            columnSize += (model.data.length + 1 + 4);
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(8 + 1 + 8 + 4 + columnSize);
        byteBuffer.put(flag);
        byteBuffer.putLong(offset);
        byteBuffer.putLong(version);
        byteBuffer.putInt(columnSize);
        for (HDatabaseColumnModel model : columnModels) {
            byteBuffer.put(model.toByteArray());
        }
        return byteBuffer.array();
    }


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Map<String, HDatabaseColumnModel> getData() {
        return data;
    }

    public void setData(Map<String, HDatabaseColumnModel> data) {
        this.data = data;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

}
