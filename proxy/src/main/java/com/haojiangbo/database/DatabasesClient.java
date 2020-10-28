package com.haojiangbo.database;

import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.container.EventClientEngineContainner;
import com.haojiangbo.hander.EventClientHander;
import com.haojiangbo.inteface.Container;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.router.SQLRouter;
import com.haojiangbo.shell.AbstractShellHander;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
/**
* @Title: DatabasesClient
* @Package com.haojiangbo.database
* @Description: 采用sql的方式配置
* @author Administrator
* @date 2020/10/28
* @version V1.0
*/
public class DatabasesClient {
    private static String sessionId = "666";
    public  void main(String [] ages){
        start(new ServerConfig(), new EventClientEngineContainner());
    }


    @SneakyThrows
    public static void start(Container... container) {
        for (Container item : container) {
            item.start();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        System.out.println("welcome to use HDatabase version 0.0.1");
        System.out.print("HDatabase>");
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = br.readLine()) != null) {
            try {

                stringBuilder.append(line);
                String tmp = stringBuilder.toString();
                if(null != tmp && tmp.length() > 0 && tmp.endsWith(";")){
                    if(!tmp.startsWith("flush")){
                        SQLRouter.router(stringBuilder.toString());
                    }else{
                        flush();
                    }
                    stringBuilder = new StringBuilder();
                }
                System.out.print("HDatabase>");
            }catch (Exception e){
                stringBuilder = new StringBuilder();
                System.out.println("error :"+e.getMessage());
                System.out.print("HDatabase>");
            }
        }
    }



    private static String flush() {
        if (EventClientHander.channel != null && EventClientHander.channel.isActive()) {
            ByteBuf send = Unpooled.wrappedBuffer(AbstractShellHander.FLUSH.getBytes());
            EventClientHander.channel.writeAndFlush(new CustomProtocol(
                    ConstantValue.DATA,
                    sessionId.getBytes().length,
                    sessionId, send.readableBytes(),
                    send));
            return AbstractShellHander.FLUSH + " 发送成功";
        } else {
            return AbstractShellHander.FLUSH + " 发送失败";
        }
    }

}
