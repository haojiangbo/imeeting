package com.haojiangbo.shell;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.haojiangbo.config.ConfigRead;
import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.constant.ConstantValue;
import com.haojiangbo.hander.EventClientHander;
import com.haojiangbo.model.ConfigModel;
import com.haojiangbo.model.CustomProtocol;
import com.haojiangbo.utils.PathUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
  *
  * 默认的shell事件处理器
 　　* @author 郝江波
 　　* @date 2020/4/18 11:18
 　　*/
public class DefaultShellHanderImp extends  ShellHanderAbstract{
    String sessionId = "134";
    @Override
    protected String flush() {
        if(EventClientHander.channel != null && EventClientHander.channel.isActive()){
            ByteBuf send = Unpooled.wrappedBuffer(ShellHanderAbstract.FLUSH.getBytes());
            EventClientHander.channel.writeAndFlush(new CustomProtocol(
                    ConstantValue.DATA,
                    sessionId.getBytes().length ,
                    sessionId, send.readableBytes(),
                    send));
            return ShellHanderAbstract.FLUSH + " 发送成功";
        }else{
            return ShellHanderAbstract.FLUSH + " 发送失败";
        }
    }

     @Override
     protected String exit() {
         return ShellHanderAbstract.EXIT;
     }

    @Override
    protected String get(String... str) {
        List<String>  cli = new ArrayList();
        getCli(cli, str);
        if(cli.size() < 2){
            return "错误的命令";
        }
        if(StringUtils.isEmpty(cli.get(1))){
            return "请输入正确的命令  比如 get A0Kxd3a1homL4N9RbMXr";
        }

        List<ConfigModel> result =  ServerConfig.INSTAND.getConfigList()
                .stream()
                .filter(item -> item.getClientId().equals(cli.get(1)))
                .collect(Collectors.toList());
        return JSONArray.toJSONString(result, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
    }

    @Override
    protected String getlist() {
        return JSONArray.toJSONString(ServerConfig.INSTAND.getConfigList(), SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
    }

    @Override
    protected String set(String... str) {
        List<String>  cli = new ArrayList();
        getCli(cli, str);
        if(cli.size() < 3){
            return "错误的命令";
        }
        switch (cli.size()){
            //修改clientId
            case 3:
                return setCilentId(cli.get(1),cli.get(2));
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
        }

        return null;
    }


    private String setCilentId(String clientId,String newClientId){
       return readLine((line, randomAccessFile,index) -> {
            String temp =  line.replace(clientId, newClientId);
            try {
                randomAccessFile.seek(0);
                for(int i = 0; i < randomAccessFile.length(); i++){
                    //randomAccessFile.writeBytes(" ");
                    randomAccessFile.writeByte(0);
                }
                randomAccessFile.seek(0);
                randomAccessFile.writeBytes(temp);
                randomAccessFile.close();
            } catch (IOException e) {
                //e.printStackTrace();
                return e.getMessage();
            }
            return "OK";
        });
    }

    private int getSeekValue(String clientId,String newClientId) {
        return Math.abs(clientId.getBytes().length - newClientId.getBytes().length) - 3;
    }


    private void getCli(List cli, String[] str) {
        for (String item : str) {
            if (!StringUtils.isEmpty(item)) {
                cli.add(item);
            }
        }
    }


    interface  Call{
        String call(String line,RandomAccessFile randomAccessFile,int index);
    }


    @SneakyThrows
    private String  readLine(Call call){
        String path = PathUtils.getPath(ServerConfig.class);
        String configPath = path+ File.separator+ ConfigRead.CONFIG_FILE_NAME;
        RandomAccessFile randomAccessFile = new RandomAccessFile(configPath,"rw");
        String line;
        int index = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = randomAccessFile.readLine()) != null){
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        return call.call(stringBuilder.toString(),randomAccessFile,index);
    }
}




