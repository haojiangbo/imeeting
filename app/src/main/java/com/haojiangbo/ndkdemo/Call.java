package com.haojiangbo.ndkdemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haojiangbo.application.MyApplication;
import com.haojiangbo.audio.AudioRecorder;
import com.haojiangbo.eventbus.CallReplyModel;
import com.haojiangbo.eventbus.MessageModel;
import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.protocol.ControlProtocol;
import com.haojiangbo.net.protocol.Pod;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.utils.android.StatusBarColorUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.UUID;

/**
 * 切换播放模式
 * https://blog.csdn.net/u010936731/article/details/70599482
 */
public class Call extends AppCompatActivity implements View.OnClickListener {
    // 发送
    public static final byte attack = 1;
    // 接受
    public static final byte accept = 2;


    private  volatile int runloding = 1;
    MediaPlayer mediaPlayer = null;
    TextView numberShowText,callStatusShow;
    String src,dst,key;
    Button acceptCall,hangCall;
    byte type = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        ActionBar actionBar = getSupportActionBar();
        if(null !=  actionBar){
            actionBar.hide();
        }
        // 初始化
        EventBus.getDefault().register(this);
        StatusBarColorUtils.setBarColor(this,R.color.heise,false);
        numberShowText = findViewById(R.id.number_show_text);
        callStatusShow = findViewById(R.id.call_status_show);
        acceptCall  = findViewById(R.id.accept_call);
        hangCall = findViewById(R.id.hang_call);


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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mssageEventBus(final CallReplyModel callReplyModel)  {
        //ToastUtils.showToastShort("CallReply收到消息");
        if(callReplyModel.type == ControlProtocol.CALL_REPLY){
            runloding = 0;
            mediaPlayer.stop();
            callStatusShow.setText("通话已连接");
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
                        if(totalNumber >= 1000 * 10){
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


    @Override
    protected void onDestroy() {
        hangCall();
        EventBus.getDefault().unregister(this);
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
                acceptCall.setVisibility(View.GONE);
                AudioRecorder.getInstance().startRecord();
                break;
        }
    }
}