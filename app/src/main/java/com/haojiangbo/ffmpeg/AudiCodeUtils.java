package com.haojiangbo.ffmpeg;

/**
 * 音频编解码器
 */
public class AudiCodeUtils {


    public native void initEncode();

    public native byte[] encodeFrame(byte[] bytes);

    public native void  freeEncode();
}
