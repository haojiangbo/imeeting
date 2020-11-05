package com.haojiangbo.thread;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DataBaseRuntimeThreadPool {
   private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 5, 100, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(3));


   public static void exec(Runnable runnable){
       executor.execute(runnable);
   }



   public static void shutdown(){
       executor.shutdown();
   }

}
