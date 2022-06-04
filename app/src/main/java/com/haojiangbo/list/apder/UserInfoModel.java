package com.haojiangbo.list.apder;

import com.haojiangbo.audio.AudioTrackManager;
import com.haojiangbo.widget.VideoSurface;

public class UserInfoModel {
    private String uid;
    private String name;
    private boolean isCurrent;
    private VideoSurface videoSurface;
    private AudioTrackManager audioTrackManager;

    public VideoSurface getVideoSurface() {
        return videoSurface;
    }

    public void setVideoSurface(VideoSurface videoSurface) {
        this.videoSurface = videoSurface;
    }

    public AudioTrackManager getAudioTrackManager() {
        return audioTrackManager;
    }

    public void setAudioTrackManager(AudioTrackManager audioTrackManager) {
        this.audioTrackManager = audioTrackManager;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
