package com.haojiangbo.ndkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.netty.channel.ChannelHandlerContext;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.application.MyApplication;
import com.haojiangbo.audio.AudioRecorder;
import com.haojiangbo.eventbus.MessageModel;
import com.haojiangbo.net.MediaProtocolManager;
import com.haojiangbo.net.protocol.ControlProtocol;
import com.haojiangbo.net.protocol.Pod;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.service.AudioPalyService;
import com.haojiangbo.storage.NumberStorageManager;
import com.haojiangbo.thread.MeidaParserInstand;
import com.haojiangbo.thread.ParserMediaProtoThreadPool;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.utils.aes.AesEncodeUtil;
import com.haojiangbo.utils.android.StatusBarColorUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

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
 *
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
 * 样式无法改变的问题
 * https://q.cnblogs.com/q/129915/
 *
 * 编解码  实例代码 都在 github上了
 * https://github.com/FFmpeg/FFmpeg/tree/master/doc/examples
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // 自己的号码
    public static String CALL_NUMBER = null;
    // 对方的号码
    public static String TARGET_NUMBER = null;

    // 案件缓存
    private static List<String> KEY_WORD_CATCH =  new ArrayList<>(6);

    // Used to load the 'native-lib' library on application startup.
    static {
        // avutil avresample swresample swscale avcodec avformat avfilter avdevice
        System.loadLibrary("native-lib");
    }

    private TextView numberShow;
    private TextView myCallNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if(null !=  actionBar){
            actionBar.hide();
        }
        StatusBarColorUtils.setBarColor(this,R.color.design_default_color_background);
        EventBus.getDefault().register(this);
        numberShow = findViewById(R.id.number_show);
        myCallNumber = findViewById(R.id.my_call_number);
        // 初始化权限
        if(!initPermission()){
           return;
        }
        // 初始化音频录制
        initAudioRecorder();
        // 开启音频播放
        startAudioPlay();
        // 获取配置信息
        initConfigInfo();
        // 初始化网络配置
        initNetworkConfig();
    }

    private void initNetworkConfig(){
        // tcp初始化
        ControlProtocolManager controlProtocolManager =  new ControlProtocolManager();
        controlProtocolManager.start();

        // udp初始化
        MediaProtocolManager mediaProtocolManager = new MediaProtocolManager();
        mediaProtocolManager.start();
    }


    private void initConfigInfo(){
        byte[] bytes = NumberStorageManager.build().getData("number.cnf");
        if (null != bytes) {
            String data = AesEncodeUtil.decode(new String(bytes));
            myCallNumber.setText("我的号码："+data);
            MainActivity.CALL_NUMBER = data;
        }
    }
    private void initAudioRecorder() {
        AudioRecorder.getInstance().createDefaultAudio();
    }
    private void startAudioPlay(){
        //Intent intent = new Intent(this,AudioPalyService.class);
        //startService(intent);
        // 启动接受音频数据的队列
        ParserMediaProtoThreadPool.exec(new MeidaParserInstand());
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mssageEventBus(final MessageModel message)  {
        ToastUtils.showToastShort("收到呼叫消息");
        message.channel.writeAndFlush(message.payLoad);
        AudioRecorder.getInstance().startRecord();
    }


    public void printRet(int t){
        Log.e("HJB>>>>>","call back"+t);
    }

    public native void startMp3(String url);




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_recorder:
                AudioRecorder.getInstance().startRecord();
                break;
            case R.id.end_recorder:
                AudioRecorder.getInstance().stopRecord();
                break;
            case R.id.register_number:
                Intent intent = new Intent(this,RigisterNumber.class);
                startActivity(intent);
                break;
            default:
                viewHander(v);
        }
    }

    private void viewHander(View view){
        if(view instanceof  Button){
            Button button  = (Button) view;
            String keyWord =  button.getText().toString();

            switch (keyWord){
                case "CALL":
                    String result =  list2str(KEY_WORD_CATCH);
                    if(result.getBytes().length != 6){
                        Toast.makeText(MyApplication.getContext(),"仅支持6位电话号码",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent call =  new Intent(this,Call.class);
                    call.putExtra("src",MainActivity.CALL_NUMBER);
                    call.putExtra("dst",result);
                    startActivity(call);
                    break;
                case "DEL":
                    if(KEY_WORD_CATCH.size() > 0){
                        KEY_WORD_CATCH.remove(KEY_WORD_CATCH.size() - 1);
                    }
                    break;
                default:
                    KEY_WORD_CATCH.add(keyWord);
            }
            if(KEY_WORD_CATCH.size() > 6){
                KEY_WORD_CATCH.remove(KEY_WORD_CATCH.size() - 1);
                Toast.makeText(MyApplication.getContext(),"最大支持6位电话号码",Toast.LENGTH_SHORT).show();
            }
            String result =  list2str(KEY_WORD_CATCH);
            numberShow.setText(result);
        }
    }

    private <T> String list2str(List<T> list){
        StringBuilder stringBuilder = new StringBuilder();
        for(T item : list){
            stringBuilder.append(item);
        }
        return stringBuilder.toString();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



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

}