package com.haojiangbo.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.audiofx.AcousticEchoCanceler;
import android.util.Log;

import com.haojiangbo.application.MyApplication;

/**
 * 回音消除
 * https://blog.csdn.net/badongdyc/article/details/73555007
 *
 *
 * PCM数据播放
 * 参考博客
 * https://www.jianshu.com/p/632dce664c3d
 */
public class AudioTrackManager {
    private AudioTrack mAudioTrack;
    private volatile static AudioTrackManager mInstance;
    //音频流类型
    /**
     * AudioManager.STREAM_MUSIC：用于音乐播放的音频流。
     * AudioManager.STREAM_SYSTEM：用于系统声音的音频流。
     * AudioManager.STREAM_RING：用于电话铃声的音频流。
     * AudioManager.STREAM_VOICE_CALL：用于电话通话的音频流。
     * AudioManager.STREAM_ALARM：用于警报的音频流。
     * AudioManager.STREAM_NOTIFICATION：用于通知的音频流。
     * AudioManager.STREAM_BLUETOOTH_SCO：用于连接到蓝牙电话时的手机音频流。
     * AudioManager.STREAM_SYSTEM_ENFORCED：在某些国家实施的系统声音的音频流。
     * AudioManager.STREAM_DTMF：DTMF音调的音频流。
     * AudioManager.STREAM_TTS：文本到语音转换（TTS）的音频流。
     * 为什么分那么多种类型，其实原因很简单，比如你在听music的时候接到电话，这个时候music播放肯定会停止，此时你只能听到电话，如果你调节音量的话，这个调节肯定只对电话起作用。当电话打完了，再回到music，你肯定不用再调节音量了。
     *
     * 作者：安仔夏天勤奋
     * 链接：https://www.jianshu.com/p/632dce664c3d
     * 来源：简书
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     */
    private static final int mStreamType = AudioManager.STREAM_VOICE_CALL;
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
    private AcousticEchoCanceler acousticEchoCanceler;

    public AudioTrackManager() {
        initData(mStreamType);
    }

    private void  initData(int streamType){
        //根据采样率，采样精度，单双声道来得到frame的大小。
        mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat);//计算最小缓冲区
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        //创建AudioTrack
        if(AudioRecorder.audioSessionId != -1){
            mAudioTrack = new AudioTrack(streamType, mSampleRateInHz,mChannelConfig,
                    mAudioFormat,mMinBufferSize,mMode,AudioRecorder.audioSessionId);
            //initAEC();
        }else{
            mAudioTrack = new AudioTrack(streamType, mSampleRateInHz,mChannelConfig,
                    mAudioFormat,mMinBufferSize,mMode);
        }
        //
        /*AudioManager audioManager = (AudioManager) MyApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM
                , AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND) ;
        int current = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);*/
        mAudioTrack.setVolume(0.9f);
    }

    /**
     * 此处的代码是然并卵的
     * 留着哪天有系统支持了再说吧
     * 实测小米是不行的
     */
    private void initAEC() {
        if (AcousticEchoCanceler.isAvailable()) {
            if (acousticEchoCanceler == null) {
                acousticEchoCanceler = AcousticEchoCanceler.create(AudioRecorder.audioSessionId);
                Log.d("initAEC", "initAEC: ---->" + acousticEchoCanceler + "\t" + AudioRecorder.audioSessionId);
                if (acousticEchoCanceler == null) {
                    Log.e("initAEC", "initAEC: ----->AcousticEchoCanceler create fail.");
                } else {
                    acousticEchoCanceler.setEnabled(true);
                }
            }
        }
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

    /**
     * 设置播放流模式
     */
    public void setPlayStaeam(int staeamType){
        initData(staeamType);
    }



    public void startPlay(byte [] bytes){
        int readCount = bytes.length;
        if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
            return;
        }
        if (readCount != 0 && readCount != -1) {
            //一边播放一边写入语音数据
            //判断AudioTrack未初始化，停止播放的时候释放了，状态就为STATE_UNINITIALIZED
            if(mAudioTrack.getState() == mAudioTrack.STATE_UNINITIALIZED){
                initData(mStreamType);
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
