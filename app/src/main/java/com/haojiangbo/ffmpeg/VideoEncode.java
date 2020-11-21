package com.haojiangbo.ffmpeg;

/**
 * 视频编码
 */
public class VideoEncode implements Codec{
    @Override
    public native void initContext();

    @Override
    public native byte[] encodeFrame(byte[] bytes);

    @Override
    public byte[] decodeFrame(byte[] bytes) {
        throw  new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public native void freeContext()  ;
}
