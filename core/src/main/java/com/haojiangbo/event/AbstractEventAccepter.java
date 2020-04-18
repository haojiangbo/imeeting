package com.haojiangbo.event;
import java.util.concurrent.CopyOnWriteArrayList;
 /**
  * 事件收听者抽象类实现
 　　* @author 郝江波
 　　* @date 2020/4/18 10:50
 　　*/
public abstract class AbstractEventAccepter implements  EventListener{
    /**
     * 监听者容器
     */
    public final CopyOnWriteArrayList<EventListener> ACCEPTER = new CopyOnWriteArrayList<>();

    @Override
    public void regist() {
        ACCEPTER.add(registInstand());
    }

    protected abstract AbstractEventAccepter registInstand();

}
