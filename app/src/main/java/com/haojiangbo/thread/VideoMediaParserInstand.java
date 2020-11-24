package com.haojiangbo.thread;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.camera.CameraActivity;
import com.haojiangbo.ffmpeg.AudioDecode;
import com.haojiangbo.ffmpeg.VideoDecode;
import com.haojiangbo.ndkdemo.Call;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

public class VideoMediaParserInstand implements Runnable{
    /**
     * 数据队列
     */
    public static final BlockingDeque<ByteBuf> MEDIA_DATA_QUEUE = new LinkedBlockingDeque<>();




    @Override
    public void run() {
        VideoDecode  videoDecode = new VideoDecode();
        videoDecode.initContext();
        int i = 1;
        while (i == 1) {
            try {
                ByteBuf byteBuf =  MEDIA_DATA_QUEUE.take();
                if(byteBuf.readableBytes() > 0){
                    byteBuf.readBytes(6);
                    int cameraType = byteBuf.readByte();
                    byteBuf.readByte();
                    // 注意 & 0xFF 防止高位补1 导致数据不一致
                    int b = (byteBuf.readByte() & 0xFF) << 8;
                    int l = byteBuf.readByte() & 0xFF;
                    int totalDataSize = b | l;
                    byte [] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bytes);
                    Log.e("video_负载大小>>",bytes.length + ">>>> n >>"+totalDataSize);
                    //byte [] b = videoDecode.decodeFrame(bytes);
                    if(null != Call.myVideoSurface && null != Call.myVideoSurface.mSurface){
                        videoDecode.drawSurface(Call.myVideoSurface.mSurface,bytes,cameraType);
                    }
                    //videoDecode.drawSurface(CameraActivity.videoSurface.mSurface,bytes);
                    ReferenceCountUtil.release(byteBuf);
                }
            } catch (InterruptedException e) {
                i = 0;
                e.printStackTrace();
            }
        }
    }

    public static Bitmap byteToBitmap(byte[] imgByte) {
        InputStream input = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;  //设置为原图片的1/8，以节省内存资源
        input = new ByteArrayInputStream(imgByte);
        SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
                input, null, options));  //软引用
        bitmap = (Bitmap) softRef.get();
        if (imgByte != null) {
            imgByte = null;
        }

        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bitmap;
    }

}
