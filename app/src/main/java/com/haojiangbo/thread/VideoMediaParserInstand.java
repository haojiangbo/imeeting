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
import com.haojiangbo.ffmpeg.VideoDecodeObj;
import com.haojiangbo.list.apder.UserInfoModel;
import com.haojiangbo.ndkdemo.Call;
import com.haojiangbo.ndkdemo.MettingActivite;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.widget.VideoSurface;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import io.netty.buffer.ByteBuf;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;

public class VideoMediaParserInstand implements Runnable {
    /**
     * 数据队列
     */
    public static final BlockingDeque<ByteBuf> MEDIA_DATA_QUEUE = new LinkedBlockingDeque<>();

    public static final Map<String, VideoDecodeObj> ENCODE_MAPPING = new HashMap<>();

    public static volatile int THREAD_STATE =  1;

    @Override
    public void run() {
        VideoDecodeObj videoDecode;
        while (THREAD_STATE == 1) {
            try {
                ByteBuf byteBuf = MEDIA_DATA_QUEUE.take();
                if (MEDIA_DATA_QUEUE.size() > 1000) {
                    continue;
                }
                if (byteBuf.readableBytes() > 0) {
                    byte[] number = new byte[14];
                    byteBuf.readBytes(number);
                    String session = new String(number);
                    videoDecode = ENCODE_MAPPING.get(session);
                    if (null == videoDecode) {
                        videoDecode = new VideoDecodeObj();
                        videoDecode.initContext();
                        ENCODE_MAPPING.put(session, videoDecode);
                    }
                    int cameraType = byteBuf.readByte();
                    byteBuf.readByte();
                    // 注意 & 0xFF 防止高位补1 导致数据不一致
                    int b = (byteBuf.readByte() & 0xFF) << 8;
                    int l = byteBuf.readByte() & 0xFF;
                    int totalDataSize = b | l;
                    byte[] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bytes);
                    Log.i("video_负载大小>>", bytes.length + ">>>> n >>" + totalDataSize);
                    //byte [] b = videoDecode.decodeFrame(bytes);
                    UserInfoModel model = MettingActivite.VIDEO_CACHE.get(session);
                    if (null != model && !session.equals(ControlProtocolManager.getSessionId()) && THREAD_STATE == 1) {
                        /*Log.e("test", "msurfa.id = " + videoSurface.mSurface.toString() + "decode = " + videoDecode.toString() + "session" + session);*/
                        videoDecode.drawSurface(model.getVideoSurface().mSurface, bytes, cameraType);
                    }
                }
                ReferenceCountUtil.release(byteBuf);
            } catch (Exception e) {
                THREAD_STATE = 0;
                e.printStackTrace();
            }
        }
    }

    public static void  freeContext(){
        MEDIA_DATA_QUEUE.clear();
        Set<String> stringSet =  ENCODE_MAPPING.keySet();
        for(String key : stringSet){
            ENCODE_MAPPING.get(key).freeContext();
        }
        ENCODE_MAPPING.clear();
        THREAD_STATE = 0;
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
