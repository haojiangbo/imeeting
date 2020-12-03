package com.haojiangbo.screen;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.haojiangbo.ndkdemo.R;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.widget.VideoSurface;

public class ScreenRecordActivity extends AppCompatActivity  implements View.OnClickListener {
    private static final String RECORD_STATUS = "record_status";
    private static final String SCREENCAP_NAME = "screencap";

    private static final int REQUEST_CODE = 1000;
    private static  int width = 640;
    private static  int height = 480;
    private MediaProjection mMediaProjection;
    //private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;
    public  static volatile ImageReader mImageReader;

    private SurfaceView videoSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_record);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar){
            actionBar.hide();
        }
        videoSurface = findViewById(R.id.screen_video_surface);


        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();



        WindowManager wm = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
     /*  width= size.x;
        height= size.y;
        Log.e("xxx","width:"+width+"   height："+height);*/
        Handler  childHandler = new Handler(handlerThread.getLooper());
        mImageReader = ImageReader.newInstance(width,height , PixelFormat.RGBA_8888,2);

        startScreenRecording();
    }

    /**
     * 获取屏幕录制的权限
     */
    private void startScreenRecording() {
        // TODO Auto-generated method stub
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, REQUEST_CODE);
    }

    private MediaProjection createMediaProjection(int resultCode,Intent data) {
        return ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE)).getMediaProjection(resultCode, data);
    }


    private VirtualDisplay createVirtualDisplay() {

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        VirtualDisplay r = mMediaProjection.createVirtualDisplay(SCREENCAP_NAME, width,height , metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,mImageReader.getSurface(), null, null);
        // https://blog.csdn.net/qq_36332133/article/details/99680797
        //  https://www.jianshu.com/p/c4ea60bc73d2
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.e("screen data",">>>>>");
                reader.close();
            }
        },new Handler(getMainLooper()));
        return r;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaProjection.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                mMediaProjection =  createMediaProjection(resultCode,data);
                ToastUtils.showToastShort("录制创建");
            } else {
                Toast.makeText(this, "取消了录制取消", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        ToastUtils.showToastShort("开始录制");
        switch (v.getId()){
            case  R.id.start_recold_screen:
                createVirtualDisplay();
                break;
        }
    }
}