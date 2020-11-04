package com.haojiangbo.datamodel;

import lombok.Data;

@Data
public class MetaDataModel {
    private String name;
    private byte type;

    public MetaDataModel(String name, byte type) {
        this.name = name;
        this.type = type;
    }
}
