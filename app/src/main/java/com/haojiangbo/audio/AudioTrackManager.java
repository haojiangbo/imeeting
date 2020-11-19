package com.haojiangbo.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class AudioTrackManager {
    private AudioTrack mAudioTrack;
    private DataInputStream mDis;//播放文件的数据流
    private Thread mRecordThread;
    private boolean isStart = false;
    private volatile static AudioTrackManager mInstance;

    //音频流类型
    private static final int mStreamType = AudioManager.STREAM_MUSIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz=44100 ;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig= AudioFormat.CHANNEL_OUT_MONO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat=AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mMinBufferSize;
    //STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。这个和我们在socket中发送数据一样，
    // 应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。
    private static int mMode = AudioTrack.MODE_STREAM;


    public AudioTrackManager() {
        initData();
    }

    private void initData(){
        //根据采样率，采样精度，单双声道来得到frame的大小。
        mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat);//计算最小缓冲区
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        //创建AudioTrack
        mAudioTrack = new AudioTrack(mStreamType, mSampleRateInHz,mChannelConfig,
                mAudioFormat,mMinBufferSize,mMode);
    }


    /**
     * 获取单例引用
     *
     * @return
     */
    public static AudioTrackManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioTrackManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioTrackManager();
                }
            }
        }
        return mInstance;
    }



    public void startPlay(byte [] bytes){
        int readCount = bytes.length;
        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
            return;
        }
        if (readCount != 0 && readCount != -1) {//一边播放一边写入语音数据
            //判断AudioTrack未初始化，停止播放的时候释放了，状态就为STATE_UNINITIALIZED
            if(mAudioTrack.getState() == mAudioTrack.STATE_UNINITIALIZED){
                initData();
            }
            mAudioTrack.play();
            mAudioTrack.write(bytes, 0, readCount);
        }
    }




    /**
     * 停止播放
     */
    public void stopPlay() {
        try {
            if (mAudioTrack != null) {
                if (mAudioTrack.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                    mAudioTrack.stop();//停止播放
                }
                if (mAudioTrack != null) {
                    mAudioTrack.release();//释放audioTrack资源
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
