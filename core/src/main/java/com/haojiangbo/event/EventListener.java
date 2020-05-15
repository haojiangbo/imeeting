package com.haojiangbo.event;

import com.haojiangbo.model.EventMessage;

import java.util.concurrent.CopyOnWriteArrayList;

/**
  * 事件通知
 　　* @author 郝江波
 　　* @date 2020/4/18 10:36
 　　*/
public interface EventListener {
    /**
     * 监听者容器
     */
    CopyOnWriteArrayList<EventListener> ACCEPTER = new CopyOnWriteArrayList<>();

    /**
     * notifyEvent
     * @param eventMessage
     */
    void notifyEvent(EventMessage eventMessage);
}
