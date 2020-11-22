package com.haojiangbo.thread;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ParserMediaProtoThreadPool {

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 100, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(3));


   public static void exec(Runnable runnable){
       executor.execute(runnable);
   }


   public static void shutdown(){
       executor.shutdown();
   }

}
