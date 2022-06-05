package com.haojiangbo.ndkdemo;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.haojiangbo.audio.AudioRecorder;
import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.camera.CameraActivity;
import com.haojiangbo.ffmpeg.VideoEncode;
import com.haojiangbo.list.apder.UserInfoModel;
import com.haojiangbo.list.apder.VideoItemListApader;
import com.haojiangbo.net.MediaProtocolManager;
import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.protocol.MediaDataProtocol;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.net.tcp.hander.IdleCheckHandler;
import com.haojiangbo.thread.MeidaParserInstand;
import com.haojiangbo.thread.VideoMediaParserInstand;
import com.haojiangbo.utils.ImageUtil;
import com.haojiangbo.utils.SessionUtils;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.utils.android.StatusBarColorUtils;
import com.haojiangbo.widget.VideoSurface;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.DatagramPacket;

public class MettingActivite extends AppCompatActivity implements View.OnClickListener {

    GridLayout videoContainerLayout = null;
    RecyclerView videoContainerList = null;
    public static final Map<String, UserInfoModel> VIDEO_CACHE = new ConcurrentHashMap<>();
    public static volatile VideoSurface current = null;
    VideoEncode videoEncode = null;
    SurfaceHolder mSurfaceHolder;
    private static MettingActivite INSTAND = null;
    VideoItemListApader videoItemListApader = new VideoItemListApader();
    TextView videoRoomName, roomAudioType;
    ImageView videoTypeBut, audioTypeBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metting_activite);
        ActionBar actionBar = getSupportActionBar();
        StatusBarColorUtils.setBarColor(this, R.color.heise);
        if (null != actionBar) {
            actionBar.hide();
        }
        INSTAND = this;
        videoContainerLayout = findViewById(R.id.video_container_layout);
        videoRoomName = findViewById(R.id.video_room_name);
        videoTypeBut = findViewById(R.id.video_type_but);
        audioTypeBut = findViewById(R.id.audit_type_but);
        roomAudioType = findViewById(R.id.room_audio_type);
        audioTypeBut.setOnClickListener(this);
        videoTypeBut.setOnClickListener(this);
        initVideoGroupView();
        Intent intent = getIntent();
        ArrayList<String> uids = intent.getStringArrayListExtra("meetingUids");
        if (null == uids || uids.size() == 0) {
            uids.add(ControlProtocolManager.getSessionId());
        }
        SessionUtils.Model model = SessionUtils.splitSession(uids.get(0));
        videoRoomName.setText("房间号：" + model.key);
        addVideoSurface(uids, true);
        initVideoEncode();
        initCamera();
        // AudioTrackManager.getInstance().setPlayStaeam(AudioManager.STREAM_MUSIC);
    }


    private void startMediaPlay() {
        // 启动接受音频数据的队列
        VideoMediaParserInstand.THREAD_STATE = 1;
        MeidaParserInstand.THREAD_STATE = 1;
        new Thread(new MeidaParserInstand()).start();
        new Thread(new VideoMediaParserInstand()).start();
    }

    void initVideoGroupView() {
        videoContainerList = findViewById(R.id.video_container_list);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
//        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);   //设置后瀑布流不显示了
        videoContainerList.setLayoutManager(layoutManager);
        videoContainerList.setItemAnimator(null);
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        videoContainerList.addItemDecoration(decoration);


        videoContainerList.setAdapter(videoItemListApader);
    }


    @Override
    protected void onResume() {
        startMediaPlay();
        // 开始音频录制
        AudioRecorder.getInstance().startRecord();
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        Log.e("event", "1");
        VideoMediaParserInstand.freeContext();
        MeidaParserInstand.freeContext();
        AudioRecorder.getInstance().stopRecord();
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        Log.e("event", "2");


        INSTAND = null;
        current = null;
        Set<String> stringSet = VIDEO_CACHE.keySet();
        for (String key : stringSet) {
            UserInfoModel model = VIDEO_CACHE.get(key);
            if (null != model) {
                model.getAudioTrackManager().stopPlay();
                videoContainerLayout.removeView(model.getVideoSurface().getGroupView());
            }
        }
        VIDEO_CACHE.clear();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        closeCamera();
        videoEncode.freeContext();
        super.onDestroy();
    }


    public static MettingActivite getInstand() {
        return INSTAND;
    }


    public void addVideoSurface(ArrayList<String> uids, boolean isUseCurrentView) {
        List<UserInfoModel> userInfoModels = new LinkedList<>();
        for (int i = 0; i < uids.size(); i++) {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(videoContainerLayout.getContext()).inflate(R.layout.video_item, videoContainerLayout, false);
            VideoSurface view = linearLayout.findViewById(R.id.item_video_surface);
            TextView uname = linearLayout.findViewById(R.id.item_video_uname);

            ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
            // 设置视频宽度
            setViewWidth(linearLayout, layoutParams);

            view.setGroupView(linearLayout);
            view.setSurfaceviewCorner(30);

            String sessionId = uids.get(i);
            // 生成用户对象
            UserInfoModel umodel = getUserInfoModel(isUseCurrentView, userInfoModels, view, uname, sessionId);

            UserInfoModel tmp = VIDEO_CACHE.get(sessionId);
            if (null != tmp) {
                continue;
            }
            VIDEO_CACHE.put(sessionId, umodel);
            Log.e("video cache ", sessionId);
            videoContainerLayout.addView(linearLayout);
        }
        /*videoItemListApader.setRecords(userInfoModels);
        videoItemListApader.notifyDataSetChanged();*/


    }

    @NonNull
    private UserInfoModel getUserInfoModel(boolean isUseCurrentView, List<UserInfoModel> userInfoModels, VideoSurface view, TextView uname, String sessionId) {
        UserInfoModel umodel = new UserInfoModel();
        umodel.setUid(sessionId);
        umodel.setName(umodel.getUid());
        uname.setText(umodel.getName());
        if (ControlProtocolManager.getSessionId().equals(sessionId)) {
            umodel.setCurrent(true);
        } else {
            umodel.setCurrent(false);
        }
        if (umodel.isCurrent() && null == current && isUseCurrentView) {
            current = view;
        }
        umodel.setVideoSurface(view);
        umodel.setAudioTrackManager(AudioTrackManager.getInstance(sessionId));
        umodel.getAudioTrackManager().setPlayStaeam(AudioManager.STREAM_VOICE_CALL);
        userInfoModels.add(umodel);
        return umodel;
    }

    private void setViewWidth(LinearLayout linearLayout, ViewGroup.LayoutParams layoutParams) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        // 计算宽高比 调整为合适的 宽高
        Log.i("width ", "old = " + screenWidth / 3 + "width = " + screenWidth + "heigth = " + screenHeight);
        layoutParams.width = screenWidth / 3;
        layoutParams.height = (int) (layoutParams.width * 1.5);
        linearLayout.setLayoutParams(layoutParams);
    }

    public void removeVideoSurface(String uid) {
        UserInfoModel tmp = VIDEO_CACHE.get(uid);
        VIDEO_CACHE.remove(uid);
        if (null != tmp) {
            videoContainerLayout.removeView(tmp.getVideoSurface().getGroupView());
            tmp.getAudioTrackManager().stopPlay();
        }
    }


    void initVideoEncode() {
        // 视频解码器
        videoEncode = new VideoEncode();
        videoEncode.initContext();
    }

    public void initCamera() {
        mSurfaceHolder = current.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        // mSurfaceView添加回调
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //SurfaceView创建
                // 初始化Camera
                initCamera2(CameraCharacteristics.LENS_FACING_BACK + "");
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
    private void initCamera2(String camerId) {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        childHandler = new Handler(handlerThread.getLooper());
        mainHandler = new Handler(getMainLooper());
        //后摄像头
        mCameraID = camerId;
        mImageReader = ImageReader.newInstance(640, 480, ImageFormat.YUV_420_888, 10);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() { //可以在这里处理拍照得到的临时照片 例如，写入本地
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                // Log.e("img",">>>>>"+image.getWidth()+">>>>"+image.getHeight());
                /*Rect crop = new Rect(0, 0, 640, 480);
                image.setCropRect(crop);*/
                long nowTime = System.currentTimeMillis();
                byte[] data = ImageUtil.getDataFromImage(image, ImageUtil.COLOR_FormatI420);
                int oldDataLen = data.length;
                byte[] converData = videoEncode.encodeFrame(data);
                // 发送数据
                sendPacketMessage(oldDataLen, converData);
                Log.i("use_time", System.currentTimeMillis() - nowTime + ">");
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
        if (null != converData) {
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
            int camareType = Integer.parseInt(mCameraID) << 24;
            // 服务端最大接受65535个字节 2 位足够表示了
            int dataSizeBit = converData.length & 0xFFFF;
            mediaDataProtocol.dataSize = camareType | dataSizeBit;
            mediaDataProtocol.data = converData;

            //发送视频数据
            DatagramPacket datagramPacket = new DatagramPacket(MediaDataProtocol
                    .mediaDataProtocolToByteBuf(MediaProtocolManager.CHANNEL,
                            mediaDataProtocol), new InetSocketAddress(NettyKeyConfig.getHOST(), NettyKeyConfig.getPORT()));
            if (!MediaProtocolManager.CHANNEL.isActive()) {
                Log.e("错误", "连接已关闭");
            } else {
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

    private void closeCamera() {
        if (null != mImageReader) {
            mImageReader.close();
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
        }
        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.close();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_type_but:
                closeCamera();
                if (mCameraID.equals("1")) {
                    mCameraID = "0";
                } else {
                    mCameraID = "1";
                }
                initCamera2(mCameraID);
                break;
            case R.id.audit_type_but:
                if (roomAudioType.getText().toString().equals("听筒")) {
                    roomAudioType.setText("扬声器");
                    Set<String> stringSet = VIDEO_CACHE.keySet();
                    for (String key : stringSet) {
                        VIDEO_CACHE.get(key).getAudioTrackManager().setPlayStaeam(AudioManager.STREAM_MUSIC);
                    }
                } else {
                    roomAudioType.setText("听筒");
                    Set<String> stringSet = VIDEO_CACHE.keySet();
                    for (String key : stringSet) {
                        VIDEO_CACHE.get(key).getAudioTrackManager().setPlayStaeam(AudioManager.STREAM_VOICE_CALL);
                    }
                }
                break;
        }
    }


}