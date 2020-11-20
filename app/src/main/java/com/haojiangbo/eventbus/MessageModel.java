package com.haojiangbo.eventbus;

import io.netty.channel.Channel;

public class MessageModel {
    public static final byte CALL = 1;
    public byte type;
    public Object payLoad;
    public Channel channel;

    public MessageModel(){}
    public MessageModel(byte type, Object payLoad,Channel channel) {
        this.type = type;
        this.payLoad = payLoad;
        this.channel = channel;
    }
}
