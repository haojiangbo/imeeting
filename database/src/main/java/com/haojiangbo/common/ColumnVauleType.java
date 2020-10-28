package com.haojiangbo.common;

/**
 * 列值得类型
 * 　　* @author 郝江波
 * 　　* @date 2020/10/26 14:58
 *
 */
public enum ColumnVauleType {
    INT(0),
    BIGINT(1),
    CHAR(2),
    VARCHAR(3),
    TEXT(4),
    DATE(5);

    private byte value;

    /**
     * 注意 int 大小端的问题
     *
     * @param value
     */
    ColumnVauleType(int value) {
        this.value = (byte) value;
    }

    public byte getValue() {
        return value;
    }

    /**
     * 根据字节枚举类型
     * @param b
     * @return
     */
    public static String caseByteStringValue(byte b){
        for(ColumnVauleType item : ColumnVauleType.values()){
            if(item.getValue() == b){
                return item.name();
            }
        }
        return "未知类型";
    }

}
