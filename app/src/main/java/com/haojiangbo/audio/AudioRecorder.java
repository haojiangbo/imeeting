package com.haojiangbo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.haojiangbo.ffmpeg.AudioEncode;
import com.haojiangbo.net.NettpUdpClientUtils;

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
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
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

    //文件名
    private String fileName;

    //录音文件
    private List<String> filesName = new ArrayList<>();


    private OutputStream outputStream;


    /**
     * 类级的内部类，也就是静态类的成员式内部类，该内部类的实例与外部类的实例
     * 没有绑定关系，而且只有被调用时才会装载，从而实现了延迟加载
     */
    private static class AudioRecorderHolder {
        /**
         * 静态初始化器，由JVM来保证线程安全
         */
        private static AudioRecorder instance = new  AudioRecorder();
    }

    private AudioRecorder() {

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
        NettpUdpClientUtils.init();
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

       new Thread(new Runnable() {
            @Override
            public void run() {
                //将录音状态设置成正在录音状态
                status = Status.STATUS_START;
                // new一个byte数组用来存一些字节数据，大小为缓冲区大小
                byte[] audiodata = new byte[bufferSizeInBytes];
                int totalLen = 0;
                while (status != Status.STATUS_STOP) {
                   int readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
                    totalLen += readsize;
                    Log.e("maoc   data    size>>>>", " >>>"+bufferSizeInBytes);
                    Log.e("read   data    size>>>>", " >>>"+readsize);
                    //Log.e("totalLen>>>>", " >>>"+totalLen);
                    byte [] tmpByte = new byte[readsize];
                    System.arraycopy(audiodata,0,tmpByte,0,readsize);
                    long startTime = System.nanoTime();
                    byte[] converData =  audioEncode.encodeFrame(tmpByte);
                    Log.e("useTime ",(System.nanoTime() - startTime) + " us ");
                    if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                        ByteBuf byteBuf =  NettpUdpClientUtils.CHANNEL.config().getAllocator().directBuffer(converData.length);
                        byteBuf.writeBytes(converData,0,converData.length);
                        DatagramPacket datagramPacket = new DatagramPacket(byteBuf,new InetSocketAddress(NettpUdpClientUtils.HOST,NettpUdpClientUtils.PORT));
                        NettpUdpClientUtils.CHANNEL.writeAndFlush(datagramPacket);
                    }
                    try {
                        outputStream.write(converData,0,converData.length);
                        Thread.sleep(15);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                audioEncode.freeContext();
            }
        }).start();
    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (status != Status.STATUS_START) {
            throw new IllegalStateException("没有在录音");
        } else {
            audioRecord.stop();
            status = Status.STATUS_PAUSE;
        }
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
        fileName = null;
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }

        status = Status.STATUS_NO_READY;
    }


    /**
     * 获取录音对象的状态
     *
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 获取本次录音文件的个数
     *
     * @return
     */
    public int getPcmFilesCount() {
        return filesName.size();
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
