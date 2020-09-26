package com.haojiangbo.alloc;


import io.netty.channel.Channel;
import io.netty.channel.DefaultMaxMessagesRecvByteBufAllocator;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * 限速管理 ByteBufAllocator
 * 实现原理  参考源码 AbstractNioByteChannel 的  public final void read() 方法
 * allocHandle 管理了 每次自动申请  byteBuf 的大小
 * 如果 guess 则会break 这一次循环
 * 如果1秒内读取的数据超过 限制的 byte 大小
 * 可以返回 0
 * 一直不读取 tcp 缓冲区就会占满  从而导致 tcp windowSize 为 0
 * 实现限速
 * @author 郝江波
 * @date 2020/9/21 10:28
 */
@Slf4j
public class MyLimitByteBufAllocator extends DefaultMaxMessagesRecvByteBufAllocator {


    /**
     *  限制时间 1s
     */
    public static final AttributeKey<Long> LIMIT_TIME = AttributeKey.newInstance("LIMIT_TIME");
    public static final long TIME_1S = 1000;


    private static final int DEFAULT_LIMIT = 1024;

    private HandleImpl handle;

    @Override
    public Handle newHandle() {
        if (this.handle == null) {
            this.handle = new HandleImpl(MyLimitByteBufAllocator.DEFAULT_LIMIT);
        }
        return this.handle;
    }

    public HandleImpl getHandle() {
        return handle;
    }


    public final class HandleImpl extends MaxMessageHandle {
        private  int limit;
        private volatile int readByteLength = 0;
        private  Channel channel;

        public HandleImpl(int limit) {
            this.limit = limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        @Override
        public int guess() {
            int tmp = this.limit;
            if(null == channel){
                return tmp;
            }
            this.readByteLength += this.lastBytesRead();
            if(this.readByteLength < this.limit){
                return tmp;
            }
            Long old =  channel.attr(LIMIT_TIME).get();
            Long now = System.currentTimeMillis();
            if(null == old || now - old >= TIME_1S){
                this.readByteLength = 0;
            //  此处调试用  有空指针 bug
            //  if(null != old){
            //    log.info("解除限制 wait   {} S",(now - old)/1000);
            //  }
                log.info("分配大小 malloc {} KB",this.limit/1024);
                channel.attr(LIMIT_TIME).set(now);
            }else{
                tmp = 0;
            }
            return tmp;
        }
    }


}
