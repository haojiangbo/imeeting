package com.haojiangbo.utils;

import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.datamodel.HDatabaseTableModel;
import com.haojiangbo.datamodel.MetaDataModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MetaDataUtils {

    /**
     * 元数据有效大小
     * @param metaData
     */
    public static int  calcMetaDataSize(JSONObject metaData){
        Set<String> keys =  metaData.keySet();
        int i = 0;
        for(String key : keys){
            if(key.equals(HDatabaseTableModel.PRIMARY)){
                continue;
            }
            i++;
        }
        return i;
    }

    /**
     * 列信息
     * @param metaData
     */
    public static List<MetaDataModel> getColumnInfo(JSONObject metaData){
        Set<String> keys =  metaData.keySet();
        List<MetaDataModel> rlist = new LinkedList<>();
        for(String key : keys){
            if(key.equals(HDatabaseTableModel.PRIMARY)){
                continue;
            }
            rlist.add(new MetaDataModel(key,metaData.getByte(key)));
        }
        return rlist;
    }

}
