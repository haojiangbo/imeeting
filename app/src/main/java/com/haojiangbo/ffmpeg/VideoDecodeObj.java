package com.haojiangbo.ffmpeg;

import android.view.Surface;

public class VideoDecodeObj implements Codec{

    public long decodeContext;

    @Override
    public synchronized native void initContext() ;

    @Override
    public synchronized byte[] encodeFrame(byte[] bytes) {
        throw  new UnsupportedOperationException("不支持的操作");
    }

    @Override
    public synchronized native byte[] decodeFrame(byte[] bytes) ;

    @Override
    public synchronized native void freeContext() ;

    public synchronized native void drawSurface(Surface mSurface, byte[] bytes, int camera);
}
