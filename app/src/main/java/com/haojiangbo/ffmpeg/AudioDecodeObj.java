package com.haojiangbo.ffmpeg;

/**
 * 音频解码器
 */
public class AudioDecodeObj implements Codec{
    public long decodeContext;
    @Override
    public synchronized native void initContext();
    @Override
    public synchronized  byte[] encodeFrame(byte[] bytes) {
        throw  new UnsupportedOperationException("不支持该操作");
    }
    @Override
    public synchronized native byte[] decodeFrame(byte[] bytes);
    @Override
    public synchronized native void freeContext();
}
