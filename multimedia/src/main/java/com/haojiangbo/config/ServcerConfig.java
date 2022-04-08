package com.haojiangbo.config;

import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

/**
  *
  * 端口号硬编码到项目里
 　　* @author 郝江波
 　　* @date 2020/11/20 15:10
 　　*/
public class ServcerConfig {
    /**
     * 监听端口号
     */
    public static int LISTENER_PROT = 10087;
    /**
     * 工作线程数
     */
    public static int WORK_THREAD_NUM = (Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2))) << 1;
}
