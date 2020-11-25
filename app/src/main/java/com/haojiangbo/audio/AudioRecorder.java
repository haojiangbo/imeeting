package com.haojiangbo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.haojiangbo.ffmpeg.AudioEncode;
import com.haojiangbo.ndkdemo.MainActivity;
import com.haojiangbo.net.MediaProtocolManager;
import com.haojiangbo.net.config.NettyKeyConfig;
import com.haojiangbo.net.protocol.MediaDataProtocol;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

/**
 * https://github.com/renhui/AudioDemo/blob/master/audiodemo/src/main/java/com/renhui/audiodemo/PcmToWavUtil.java
 * 实现录音
 *
 * @author chenmy0709
 * @version V001R001C01B001
 */
public class AudioRecorder {

    AudioEncode audioEncode = new AudioEncode();

    //音频输入-麦克风
    //private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //用这个参数可以完美解决回声的问题
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 44100;
    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    //录音对象
    private AudioRecord audioRecord;
    //录音状态
    private volatile Status status = Status.STATUS_NO_READY;
    //录音文件
    private List<String> filesName = new ArrayList<>();
    // 文件输出测试
    private OutputStream outputStream;
    public static volatile  int  audioSessionId = -1;

    private static class AudioRecorderHolder {
        private static AudioRecorder instance = new  AudioRecorder();
    }

    public static AudioRecorder getInstance() {
        return AudioRecorderHolder.instance;
    }

    /**
     * 创建默认的录音对象
     */
    public void createDefaultAudio() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = 2304; /*AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);*/
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        audioSessionId = audioRecord.getAudioSessionId();
    }


    /**
     * 开始录音
     */
    public void startRecord() {
        status = Status.STATUS_READY;
        if (status == Status.STATUS_NO_READY) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (status == Status.STATUS_START) {
            throw new IllegalStateException("正在录音");
        }
        Log.d("AudioRecorder", "===startRecord===" + audioRecord.getState());
        audioRecord.startRecording();

        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp3";
        File file = new File(fileName);
        if(file.exists()){
            file.delete();
        }
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 初始化编解码器
        audioEncode.initContext();

       new Thread(() -> {
           //将录音状态设置成正在录音状态
           status = Status.STATUS_START;
           // new一个byte数组用来存一些字节数据，大小为缓冲区大小
           byte[] audiodata = new byte[bufferSizeInBytes];
           int totalLen = 0;
           while (status != Status.STATUS_STOP) {
              int readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
               totalLen += readsize;
               if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                   byte [] tmpByte = new byte[readsize];
                   System.arraycopy(audiodata,0,tmpByte,0,readsize);
                   long startTime = System.nanoTime();
                   byte[] converData =  audioEncode.encodeFrame(tmpByte);

                   MediaDataProtocol mediaDataProtocol = new MediaDataProtocol();
                   mediaDataProtocol.type = MediaDataProtocol.AUDIO_DATA;
                   mediaDataProtocol.number = MainActivity.TARGET_NUMBER.getBytes();
                   mediaDataProtocol.dataSize = converData.length;
                   mediaDataProtocol.data = converData;

                   //发送音频数据
                   DatagramPacket datagramPacket = new DatagramPacket(MediaDataProtocol
                           .mediaDataProtocolToByteBuf(MediaProtocolManager.CHANNEL,
                           mediaDataProtocol),new InetSocketAddress(NettyKeyConfig.getHOST(), NettyKeyConfig.getPORT()));
                   MediaProtocolManager.CHANNEL.writeAndFlush(datagramPacket);
               }

               try {
                   //outputStream.write(converData,0,converData.length);
                   Thread.sleep(10);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
           audioEncode.freeContext();
       }).start();
    }



    /**
     * 停止录音
     */
    public void stopRecord() {
        Log.d("AudioRecorder", "===stopRecord===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            audioRecord.stop();
            // 释放编码器
            status = Status.STATUS_STOP;
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // release();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        status = Status.STATUS_NO_READY;
    }

    /**
     * 取消录音
     */
    public void canel() {
        filesName.clear();
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
        status = Status.STATUS_NO_READY;
    }

    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }

}
