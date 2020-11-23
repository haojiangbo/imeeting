package com.haojiangbo.ndkdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.StringUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.haojiangbo.application.MyApplication;
import com.haojiangbo.audio.AudioRecorder;
import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.camera.CameraActivity;
import com.haojiangbo.eventbus.CallReplyModel;
import com.haojiangbo.eventbus.MessageModel;
import com.haojiangbo.ffmpeg.VideoEncode;
import com.haojiangbo.net.MediaProtocolManager;
import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.protocol.ControlProtocol;
import com.haojiangbo.net.protocol.MediaDataProtocol;
import com.haojiangbo.net.protocol.Pod;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.utils.ImageUtil;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.utils.android.StatusBarColorUtils;
import com.haojiangbo.widget.VideoSurface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.UUID;


public class Call extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    // 发送
    public static final byte attack = 1;
    // 接受
    public static final byte accept = 2;

    public long oldTime = 0;

    private  volatile int runloding = 1;
    MediaPlayer mediaPlayer = null;
    TextView numberShowText,callStatusShow;
    String src,dst,key;
    Button acceptCall,hangCall,checkStream;
    byte type = 0;


    //////////////////////////////////////////// videoSurface ////////////////////////////////
    private VideoSurface videoSurface;
    private SurfaceHolder mSurfaceHolder;
    public static  VideoSurface myVideoSurface;
    private CameraManager mCameraManager;//摄像头管理器
    private Handler childHandler, mainHandler;
    private String mCameraID;//摄像头Id 0 为后  1 为前
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    // 视频解码器
    VideoEncode videoEncode = new VideoEncode();
    //////////////////////////////////////////// videoSurface ////////////////////////////////



    // 电源管理对象
    // 屏幕开关
    private PowerManager localPowerManager = null;// 电源管理对象
    private PowerManager.WakeLock localWakeLock = null;// 电源锁
    public SensorManager sm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        ActionBar actionBar = getSupportActionBar();
        if(null !=  actionBar){
            actionBar.hide();
        }
        videoEncode.initContext();
        // 初始化
        EventBus.getDefault().register(this);
        StatusBarColorUtils.setBarColor(this,R.color.heise,false);
        numberShowText = findViewById(R.id.number_show_text);
        callStatusShow = findViewById(R.id.call_status_show);
        acceptCall  = findViewById(R.id.accept_call);
        hangCall = findViewById(R.id.hang_call);
        checkStream = findViewById(R.id.check_stream);
        videoSurface = findViewById(R.id.call_video_surface);
        myVideoSurface = findViewById(R.id.my_surface_show);
        // 切换音频流到耳机
        AudioTrackManager.getInstance().setPlayStaeam(AudioManager.STREAM_VOICE_CALL);

        // 初始化一些数据
        Intent intent =  getIntent();
        src = intent.getStringExtra("src");
        dst = intent.getStringExtra("dst");
        key = UUID.randomUUID().toString();
        type = intent.getByteExtra("type", (byte) (-1 & 0xFF));
        MainActivity.TARGET_NUMBER = dst;
        if(null == ControlProtocolManager.ACTIVITY_CHANNEL || !ControlProtocolManager.ACTIVITY_CHANNEL.isActive()){
            ToastUtils.showToastShort("网络故障,请重新拨打");
            this.finish();
        }
        numberShowText.setText("呼叫号码："+dst);
        // 代表是发送方，拨打电话
        if(type == Call.attack){
            // 播放音乐
            mediaPalay();
            // 呼叫loding
            initLoding();
            // 发送呼叫消息
            sendCallMessage(src, dst, key);
            acceptCall.setVisibility(View.GONE);
        }else if(type == Call.accept){
            // 播放音乐
            mediaPalay();
            numberShowText.setText("来电号码："+dst);
            callStatusShow.setVisibility(View.GONE);
        }
        // 保持屏幕常量
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //初始化距离传感器
        initRangeSensor();
        //初始化视频
        initVideoConfig();
    }


    @SuppressLint("InvalidWakeLockTag")
    private void initRangeSensor(){
        //初始化距离传感器
        //根据传入的传感器类型初始化传感器
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor acceleromererSensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        // //注册传感器，第一个参数为距离监听器，第二个是传感器类型，第三个是获取数据速度
        sm.registerListener(this, acceleromererSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        localPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        // 获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        localWakeLock = localPowerManager.newWakeLock(32, "MyPower");
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mssageEventBus(final CallReplyModel callReplyModel)  {
        //ToastUtils.showToastShort("CallReply收到消息");
        if(callReplyModel.type == ControlProtocol.CALL_REPLY){
            runloding = 0;
            mediaPlayer.stop();
            callStatusShow.setText("通话已连接");
            checkStream.setVisibility(View.VISIBLE);
            // 开始录音
            AudioRecorder.getInstance().startRecord();
        }else if(callReplyModel.type == ControlProtocol.HANG){
            ToastUtils.showToastShort("通话已挂断");
            this.finish();
        }
    }


    private void mediaPalay() {
        AudioManager audioManager = (AudioManager) MyApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(false);
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }*/
        mediaPlayer = MediaPlayer.create(this,R.raw.call);
        mediaPlayer.start();
    }

    private void initLoding(){
        String[] messageArray = new String[]{"正在呼叫.","正在呼叫..","正在呼叫..."};
        new Thread(){
            int i = 0;
            public void run(){
                int totalNumber = 0;
                while (runloding == 1){
                    try {
                        Thread.sleep(500);
                        if(runloding != 1){
                            return;
                        }
                        runOnUiThread(() -> {
                            callStatusShow.setText(messageArray[i]);
                        });
                        i++;
                        if(i == 3){
                            i = 0;
                        }
                        totalNumber += 500;
                        if(totalNumber >= 1000 * 60){
                            runloding = 0;
                            runOnUiThread(() -> {
                                ToastUtils.showToastLong("对方无人接听");
                            });
                            Thread.sleep(500);
                            runOnUiThread(() -> {
                                Call.this.finish();
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    /**
     * 发送呼叫消息消息
     * @param src
     * @param dst
     * @param key
     */
    private void sendCallMessage(String src, String dst, String key) {
        byte[] session = ControlProtocolManager.ACTIVITY_CHANNEL.attr(NettyKeyConfig.SESSION_KEY).get().getBytes();
        Pod pod = new Pod(src,dst,key);
        byte[] data = pod.toString().getBytes();
        ControlProtocol protocol = new ControlProtocol(ControlProtocol.CALL,
                (byte) session.length,
                session,
                data.length,
                data
                );
        ControlProtocolManager.ACTIVITY_CHANNEL.writeAndFlush(protocol);
    }

    // 挂断通话 发送挂断消息
    private void hangCall(){
        runloding = 0;
        mediaPlayer.stop();
        try {
            AudioRecorder.getInstance().stopRecord();
        }catch (Exception e){
            e.printStackTrace();
        }
        String session = ControlProtocolManager.ACTIVITY_CHANNEL.attr(NettyKeyConfig.SESSION_KEY).get();
        byte [] sesbyteArray =  session.getBytes();
        Pod pod = new Pod(src,dst,key);
        byte[] data = pod.toString().getBytes();
        ControlProtocol controlProtocol =  new ControlProtocol();
        controlProtocol.flag = ControlProtocol.HANG;
        controlProtocol.sessionSize = (byte) sesbyteArray.length;
        controlProtocol.session = sesbyteArray;
        controlProtocol.dataSize = data.length;
        controlProtocol.data = data;
        ControlProtocolManager.ACTIVITY_CHANNEL.writeAndFlush(controlProtocol);
        this.finish();
    }

    /**
     * 退出事件
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(oldTime == 0){
                ToastUtils.showToastShort("连续滑动2次退出哦~");
                oldTime = System.currentTimeMillis();
                return false;
            }
            if(System.currentTimeMillis() - oldTime  < 500){
                return super.onKeyDown(keyCode,event);
            }else{
                oldTime = System.currentTimeMillis();
            }
            return false;
        }
        return super.onKeyDown(keyCode,event);
    }
    @Override
    protected void onDestroy() {
        hangCall();
        EventBus.getDefault().unregister(this);
        sm.unregisterListener(this);
        if(localWakeLock != null){
            try {
                localWakeLock.release();
            }catch (Exception e){}
        }
        videoEncode.freeContext();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hang_call:
                hangCall();
                break;
            case R.id.accept_call:
                Bundle  bundle = getIntent().getBundleExtra("protocol");
                ControlProtocol protocol = (ControlProtocol) bundle.getSerializable("protocol");
                ControlProtocolManager.ACTIVITY_CHANNEL.writeAndFlush(protocol);
                mediaPlayer.stop();
                callStatusShow.setText("通话已连接");
                callStatusShow.setVisibility(View.VISIBLE);
                acceptCall.setVisibility(View.GONE);
                checkStream.setVisibility(View.VISIBLE);
                AudioRecorder.getInstance().startRecord();
                break;
            case R.id.check_stream:
                if(checkStream.getText().toString().equals("免提")){
                    checkStream.setText("听筒");
                    checkStream.setTextColor(getResources().getColor(R.color.lanse));
                    AudioTrackManager.getInstance().setPlayStaeam(AudioManager.STREAM_MUSIC);
                } else{
                    AudioTrackManager.getInstance().setPlayStaeam(AudioManager.STREAM_VOICE_CALL);
                    checkStream.setText("免提");
                    checkStream.setTextColor(getResources().getColor(R.color.white));
                }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.values[0] <= 0){
            if (localWakeLock.isHeld()) {
                return;
            } else{
                localWakeLock.acquire();// 申请设备电源锁
            }
        }else{
            if (localWakeLock.isHeld()) {
                return;
            } else{
                localWakeLock.setReferenceCounted(false);
                localWakeLock.release(); // 释放设备电源锁
            }
        }
    }


    /**
     * 视频参数配置
     */
    private void initVideoConfig() {
        videoSurface.setOnClickListener(this);
        mSurfaceHolder = videoSurface.getHolder();
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
                    Call.this.mCameraDevice = null;
                }
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        //后摄像头
        mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;
        mImageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888,10);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                byte [] data =ImageUtil.getDataFromImage(image,ImageUtil.COLOR_FormatI420);
                //byte [] data =  ImageUtil.getBytesFromImageAsType(image,ImageUtil.YUV420P);
                int oldDataLen = data.length;
                byte [] converData =  videoEncode.encodeFrame(data);
                // 发送数据
                sendPacketMessage(oldDataLen, converData);
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
                Call.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            Toast.makeText(Call.this, "摄像头开启失败", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Call.this, "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void sendPacketMessage(int oldDataLen, byte[] converData) {
        if(StringUtil.isNullOrEmpty(MainActivity.TARGET_NUMBER)){
            return;
        }
        if(null != converData){
            MediaDataProtocol mediaDataProtocol = new MediaDataProtocol();
            mediaDataProtocol.type = MediaDataProtocol.VIDEO_DATA;
            mediaDataProtocol.number = MainActivity.TARGET_NUMBER.getBytes();
            mediaDataProtocol.dataSize = converData.length;
            mediaDataProtocol.data = converData;
            //发送视频数据
            DatagramPacket datagramPacket = new DatagramPacket(MediaDataProtocol
                    .mediaDataProtocolToByteBuf(MediaProtocolManager.CHANNEL,
                            mediaDataProtocol),new InetSocketAddress(NettyKeyConfig.getHOST(), NettyKeyConfig.getPORT()));
            MediaProtocolManager.CHANNEL.writeAndFlush(datagramPacket);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.e("onAccuracyChanged", ">>>>>>"+accuracy);
    }
}