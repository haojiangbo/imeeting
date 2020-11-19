package com.haojiangbo.ndkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.haojiangbo.audio.AudioRecorder;
import com.haojiangbo.service.AudioPalyService;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * 安卓 ffmpeg 编译
 *  https://www.jianshu.com/p/0a7f3175c1b9
 *  https://www.jianshu.com/p/276abc1ffbc6
 *  参考代码
 *  https://www.jianshu.com/p/f41cfc337f93
 *
 *  JNI环境参考
 *
 *
 *  编译环境 成功跑通的
 *  https://juejin.im/post/6844903945496690696
 *
 *
 *  代码地址
 * https://github.com/coopsrc/FFPlayerDemo/tree/master/app/src/main/java/cc/dewdrop/ffplayer
 *
 *
 * 安卓如何播放 pcm 数据
 * https://blog.csdn.net/c1392851600/article/details/86532500
 *
 * 11
 * https://www.jianshu.com/p/0a7f3175c1b9
 *
 *
 * C++ 与 java 方法的互相调用
 * https://blog.csdn.net/xfhy_/article/details/82801706
 *
 * 方法签名
 * https://blog.csdn.net/u011305680/article/details/75000349
 *
 *
 * 关于安卓工具 每次都需要 download maven metadata 的问题
 * 一般都是 gradle 的依赖 有不确定的依赖版本 版本后面带 + 号的
 * 去掉就可以了
 *
 *
 * 不用下载那个，执行这个就ok了；ln -s arm-linux-androideabi-4.9 mipsel-linux-android2年前回复
    wind 下mklink /j mips64el-linux-android aarch64-linux-android-4.9
 *
 *
 *
 * 编解码  实例代码 都在 github上了
 * https://github.com/FFmpeg/FFmpeg/tree/master/doc/examples
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // Used to load the 'native-lib' library on application startup.
    static {

        // avutil avresample swresample swscale avcodec avformat avfilter avdevice
      /*  System.loadLibrary("avutil");
        System.loadLibrary("avresample");
        System.loadLibrary("swresample");
        System.loadLibrary("swscale");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("avdevice");*/
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if(null !=  actionBar){
            actionBar.hide();
        }
        if(!initPermission()){
           return;
        }
     /*   AudiCodeUtils audiCodeUtils = new AudiCodeUtils();
        audiCodeUtils.initEncode();*/
        AudioRecorder.getInstance().createDefaultAudio();
        //this.startMp3(Environment.getExternalStorageDirectory().getAbsolutePath()+"/mp3/test.mp3");

        // 开启音频播放的service
        startAudioPlayService();
    }


    private void startAudioPlayService(){
        Intent intent = new Intent(this,AudioPalyService.class);
        startService(intent);
    }


    public void printRet(int t){
        Log.e("HJB>>>>>","call back"+t);
    }




    public native void startMp3(String url);


    //////////////////////////////////////动态申请权限模块///////////////////////////////////////

    String [] quanxianList = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    /* Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
     Manifest.permission.ACCESS_FINE_LOCATION,
     Manifest.permission. READ_PHONE_STATE,*/
    private final int mRequestCode = 100;//权限请求码
    private boolean  initPermission() {
        int sta = 0;
        for(String s : quanxianList){
            if (ContextCompat.checkSelfPermission(this, s) != PERMISSION_GRANTED) {
                sta++;
            }
        }
        if(sta > 0){
            ActivityCompat.requestPermissions(this, quanxianList, mRequestCode);
        }
        return sta > 0 ? false : true;
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog();/// 跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                //this.finish();
            }
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;
    private static final int NOT_NOTICE = 2;//如果勾选了不再询问
    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            Uri packageURI = Uri.parse("package:" +  getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivityForResult(intent, NOT_NOTICE);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            MainActivity.this.finish();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }
    //关闭对话框
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_recorder:
                AudioRecorder.getInstance().startRecord();
                break;
            case R.id.end_recorder:
                AudioRecorder.getInstance().stopRecord();
                break;
        }
    }
}