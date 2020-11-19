package com.haojiangbo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.haojiangbo.net.NettpUdpClientUtils;

public class AudioPalyService extends Service {
    public AudioPalyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.e("服务被启动>>>>",">>>>>>>>>>>>>>");
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("服务被启动>>>>","端口监听");
        NettpUdpClientUtils.init();
        return super.onStartCommand(intent, flags, startId);
    }
}