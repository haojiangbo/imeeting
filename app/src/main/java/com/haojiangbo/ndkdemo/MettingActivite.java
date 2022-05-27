package com.haojiangbo.ndkdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.haojiangbo.audio.AudioRecorder;
import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.camera.CameraActivity;
import com.haojiangbo.ffmpeg.VideoEncode;
import com.haojiangbo.net.MediaProtocolManager;
import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.protocol.MediaDataProtocol;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.net.tcp.hander.IdleCheckHandler;
import com.haojiangbo.utils.ImageUtil;
import com.haojiangbo.widget.VideoSurface;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.DatagramPacket;

public class MettingActivite extends AppCompatActivity {

    LinearLayoutCompat videoContainerLayout = null;
    public  static  final Map<String,VideoSurface> videoSurfaces = new LinkedHashMap<>();
    public  static  final Map<String,AudioTrackManager> audioManager = new LinkedHashMap<>();
    VideoSurface current = null;
    public  static  VideoSurface test = null;
    VideoEncode videoEncode = null;
    SurfaceHolder mSurfaceHolder;
    private static MettingActivite instand  = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metting_activite);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }
        videoContainerLayout = findViewById(R.id.video_container_layout);
        Intent intent =  getIntent();
        ArrayList<String> uids =  intent.getStringArrayListExtra("meetingUids");
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        if(null == uids || uids.size() == 0){
            VideoSurface view = (VideoSurface) layoutInflater.inflate(R.layout.video_pod, null);
            current = view;
        }else {
            addVideoSurface(uids);
        }
        initVideoEncode();
        initCamera();

        instand = this;

        //AudioTrackManager.getInstance().setPlayStaeam(AudioManager.STREAM_VOICE_CALL);
        // AudioRecorder.getInstance().startRecord();
    }

    @Override
    protected void onDestroy() {
        instand = null;
        super.onDestroy();
    }

    public static MettingActivite getInstand(){
        return instand;
    }


    public void addVideoSurface(ArrayList<String> uids) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < uids.size(); i++) {
            String sessionId = uids.get(i);
            VideoSurface view = (VideoSurface) layoutInflater.inflate(R.layout.video_pod, null);
            VideoSurface tmp =  videoSurfaces.get(sessionId);
            if(ControlProtocolManager.getSessionId().equals(sessionId)){
                current = view;
            }
            if(null != tmp){
                continue;
            }
            videoSurfaces.put(sessionId,view);
            videoContainerLayout.addView(view, 300, 300);
            audioManager.put(sessionId,AudioTrackManager.getInstance(sessionId));
        }
    }

    void initVideoEncode() {
        // 视频解码器
        videoEncode = new VideoEncode();
        videoEncode.initContext();
    }

    void initCamera() {
        mSurfaceHolder = current.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //SurfaceView创建
                // 初始化Camera
                initCamera2();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { //SurfaceView销毁
                // 释放Camera资源
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    MettingActivite.this.mCameraDevice = null;
                }
            }
        });
    }

    CameraDevice mCameraDevice;
    private Handler childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraManager mCameraManager;//摄像头管理器
    private CameraCaptureSession mCameraCaptureSession;
    /**
     * 初始化Camera2
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        //后摄像头
        mCameraID = "" + CameraCharacteristics.LENS_FACING_BACK;
        mImageReader = ImageReader.newInstance(640,480 , ImageFormat.YUV_420_888,10);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                // Log.e("img",">>>>>"+image.getWidth()+">>>>"+image.getHeight());
                /* Rect crop = new Rect(10,10,650,970);*/
                //image.setCropRect(crop);
                long nowTime = System.currentTimeMillis();
                byte [] data =  ImageUtil.getDataFromImage(image,ImageUtil.COLOR_FormatI420);
                int oldDataLen = data.length;
                byte [] converData =  videoEncode.encodeFrame(data);
                // 发送数据
                sendPacketMessage(oldDataLen, converData);
                Log.i("use_time",System.currentTimeMillis() - nowTime+">");
                image.close();
            }
        }, childHandler);


        //获取摄像头管理
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开摄像头
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void sendPacketMessage(int oldDataLen, byte[] converData) {
        if(null != converData){
            /*try {
                outputStream.write(converData);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //Log.e("编码前数据","编码前大小"+oldDataLen + "编码后数据大小"+converData.length);
            MediaDataProtocol mediaDataProtocol = new MediaDataProtocol();
            mediaDataProtocol.type = MediaDataProtocol.VIDEO_DATA;
            mediaDataProtocol.number = ControlProtocolManager.getSessionId().getBytes();
            // 高位1字节 表示摄像头的正反
            int camareType = 0 << 24;
            // 服务端最大接受65535个字节 2 位足够表示了
            int dataSizeBit = converData.length & 0xFFFF;
            mediaDataProtocol.dataSize = camareType | dataSizeBit;
            mediaDataProtocol.data = converData;

            //发送视频数据
            DatagramPacket datagramPacket = new DatagramPacket(MediaDataProtocol
                    .mediaDataProtocolToByteBuf(MediaProtocolManager.CHANNEL,
                            mediaDataProtocol),new InetSocketAddress(NettyKeyConfig.getHOST(), NettyKeyConfig.getPORT()));
            if(!MediaProtocolManager.CHANNEL.isActive()){
                Log.e("错误","连接已关闭");
            }else {
                MediaProtocolManager.CHANNEL.writeAndFlush(datagramPacket);
            }
        }
    }


    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//打开摄像头
            mCameraDevice = camera;
            //开启预览
            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {//关闭摄像头
            if (null != mCameraDevice) {
                mCameraDevice.close();
                MettingActivite.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            Toast.makeText(MettingActivite.this, "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 开始预览
     */
    private void takePreview() {
        try {
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());

            // todo 设置实时帧数据接收
            previewRequestBuilder.addTarget(mImageReader.getSurface());
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() // ③
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MettingActivite.this, "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


}