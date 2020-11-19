package com.haojiangbo.ffmpeg;

public interface Codec {
    /**
     * 初始化编码器
     */
    void initContext();

    /**
     * 压缩PCM数据
     * @param bytes
     * @return
     */
    byte[] encodeFrame(byte[] bytes);

    /**
     * 解压缩数据
     * @param bytes
     * @return
     */
    byte[] decodeFrame(byte[] bytes);

    /**
     * 释放编码器
     */
    void  freeContext();
}
