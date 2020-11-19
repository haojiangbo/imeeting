package com.haojiangbo.protocol;

import lombok.Data;

/**
 * 该协议使用UDP传输，
 * 不考虑丢包情况
 * 也不会重新传输
 * @author Administrator
 * @version V1.0
 * @Title: MediaDataProtocol
 * @Package com.haojiangbo.protocol
 * @Description: 多媒体数据传输协议
 * @date 2020/11/19
 */
@Data
public class MediaDataProtocol {
    private byte header;
    private byte type;
    private int dataSize;
    private byte[] data;
}
