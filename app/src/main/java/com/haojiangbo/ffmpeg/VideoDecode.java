package com.haojiangbo.ffmpeg;

import android.view.Surface;

public class VideoDecode implements Codec {
    @Override
    public native void initContext() ;

    @Override
    public byte[] encodeFrame(byte[] bytes) {
       throw  new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public native byte[] decodeFrame(byte[] bytes) ;

    @Override
    public native void freeContext() ;

    public native void drawSurface(Surface mSurface,byte[] bytes);

}
