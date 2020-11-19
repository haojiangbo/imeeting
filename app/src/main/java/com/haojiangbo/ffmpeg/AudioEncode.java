package com.haojiangbo.ffmpeg;

/**
 * 音频编解码器
 */
public class AudioEncode implements Codec{
    /**
     * 初始化编码器
     */
    public native void initContext();
    /**
     * 压缩PCM数据
     * @param bytes
     * @return
     */
    public native byte[] encodeFrame(byte[] bytes);
    @Override
    public byte[] decodeFrame(byte[] bytes) {
        throw new UnsupportedOperationException("不支持的操作");
    }
    /**
     * 释放编码器
     */
    public native void  freeContext();
}
