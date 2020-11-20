package com.haojiangbo.eventbus;

import com.haojiangbo.net.protocol.ControlProtocol;

public class CallReplyModel {
    public ControlProtocol controlProtocol;
    public CallReplyModel(ControlProtocol controlProtocol) {
        this.controlProtocol = controlProtocol;
    }
}
