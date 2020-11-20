package com.haojiangbo.ndkdemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
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
public class Call extends AppCompatActivity {
    private  volatile int runloding = 1;
    MediaPlayer mediaPlayer = null;
    TextView numberShowText,callStatusShow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);  ActionBar actionBar = getSupportActionBar();
        if(null !=  actionBar){
            actionBar.hide();
        }
        StatusBarColorUtils.setBarColor(this,R.color.heise,false);
        Intent intent =  getIntent();
        String src = intent.getStringExtra("src");
        String dst = intent.getStringExtra("dst");
        MainActivity.TARGET_NUMBER = dst;
        String key = UUID.randomUUID().toString();
        if(null == ControlProtocolManager.ACTIVITY_CHANNEL || !ControlProtocolManager.ACTIVITY_CHANNEL.isActive()){
            ToastUtils.showToastShort("网络故障,请重新拨打");
            this.finish();
        }
        EventBus.getDefault().register(this);
        numberShowText = findViewById(R.id.number_show_text);
        callStatusShow = findViewById(R.id.call_status_show);
        numberShowText.setText("号码："+dst);
        // 播放音乐
        mediaPalay();
        // 呼叫loding
        initLoding();
        // 发送呼叫消息
        sendCallMessage(src, dst, key);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mssageEventBus(final CallReplyModel callReplyModel)  {
        ToastUtils.showToastShort("CallReply收到消息");
        runloding = 0;
        mediaPlayer.stop();
        callStatusShow.setText("通话已连接");
        AudioRecorder.getInstance().startRecord();
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
     * 发送消息
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




    @Override
    protected void onDestroy() {
        mediaPlayer.stop();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}