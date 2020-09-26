package com.haojiangbo.enums;

import lombok.Getter;
/**
* @Title: LmitStreamEnmu
* @Package com.haojiangbo.enums
* @Description: 限速
* @author 郝江波
* @date 2020/9/26
* @version V1.0
*/
@Getter
public enum LmitStreamEnmu {
    LIMIT_128KB("128",1024 * 128),
    LIMIT_256KB("256",1024 * 256),
    LIMIT_512KB("512",1024 * 512),
    LIMIT_1024KB("1024",1024 * 1024);

    private String name;
    private int    value;
    LmitStreamEnmu(String name, int value) {
        this.name = name;
        this.value = value;
    }
}
