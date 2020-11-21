package com.haojiangbo.eventbus;

import com.haojiangbo.net.protocol.ControlProtocol;


public class CallReplyModel {
    public byte type;
    public ControlProtocol controlProtocol;
    public CallReplyModel(byte type,ControlProtocol controlProtocol) {
        this.controlProtocol = controlProtocol;
        this.type = type;
    }
}
