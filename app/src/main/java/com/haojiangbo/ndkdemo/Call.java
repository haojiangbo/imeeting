package com.haojiangbo.ndkdemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.protocol.ControlProtocol;
import com.haojiangbo.net.protocol.Pod;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.utils.android.StatusBarColorUtils;

import java.util.UUID;

public class Call extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);  ActionBar actionBar = getSupportActionBar();
        if(null !=  actionBar){
            actionBar.hide();
        }
        StatusBarColorUtils.setBarColor(this,R.color.design_default_color_background);
        Intent intent =  getIntent();
        String src = intent.getStringExtra("src");
        String dst = intent.getStringExtra("dst");
        String key = UUID.randomUUID().toString();
        if(null == ControlProtocolManager.ACTIVITY_CHANNEL || !ControlProtocolManager.ACTIVITY_CHANNEL.isActive()){
            ToastUtils.showToastShort("网络故障,请重新拨打");
            this.finish();
        }
        sendCallMessage(src, dst, key);
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
}