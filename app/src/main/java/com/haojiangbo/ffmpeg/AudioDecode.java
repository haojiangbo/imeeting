package com.haojiangbo.ffmpeg;

/**
 * 解码器
 */
public class AudioDecode  implements Codec{

    @Override
    public native void initContext();
    @Override
    public  byte[] encodeFrame(byte[] bytes) {
        throw  new UnsupportedOperationException("不支持该操作");
    }
    @Override
    public native byte[] decodeFrame(byte[] bytes);
    @Override
    public native void freeContext();
}
