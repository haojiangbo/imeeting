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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认的shell事件处理器
 * 　　* @author 郝江波
 * 　　* @date 2020/4/18 11:18
 *
 */
public class DefaultShellHanderImp extends ShellHanderAbstract {
    String sessionId = "134";

    @Override
    protected String flush() {
        if (EventClientHander.channel != null && EventClientHander.channel.isActive()) {
            ByteBuf send = Unpooled.wrappedBuffer(ShellHanderAbstract.FLUSH.getBytes());
            EventClientHander.channel.writeAndFlush(new CustomProtocol(
                    ConstantValue.DATA,
                    sessionId.getBytes().length,
                    sessionId, send.readableBytes(),
                    send));
            return ShellHanderAbstract.FLUSH + " 发送成功";
        } else {
            return ShellHanderAbstract.FLUSH + " 发送失败";
        }
    }

    @Override
    protected String exit() {
        return ShellHanderAbstract.EXIT;
    }

    @Override
    protected String get(String... str) {
        List<String> cli = new ArrayList();
        getCli(cli, str);
        if (cli.size() < 2) {
            return "错误的命令";
        }
        if (StringUtils.isEmpty(cli.get(1))) {
            return "请输入正确的命令  比如 get A0Kxd3a1homL4N9RbMXr";
        }

        List<ConfigModel> result = ServerConfig.INSTAND.getConfigList()
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
        List<String> cli = new ArrayList();
        getCli(cli, str);
        if (cli.size() < 4) {
            return "错误的命令 请参考文档";
        }
        return setHander(cli.get(1), cli.get(2), cli.get(3));
    }


    private String setHander(String clientId, String feild, String value) {
        return readLine((line, randomAccessFile, strings) -> {
            String temp = strings
                    .stream()
                    .filter(item -> item.contains(clientId))
                    .findFirst().orElse(null);
            if (null == temp) {
                return "clientId 不存在";
            }

            int index = filedParser(feild);
            if (index == -1) {
                return feild + "不是一个正确的 field 名称";
            }

            //覆盖值
            String[] v = valueParser(temp);
            temp = line.replace(temp, temp.replace(v[index],index == v.length - 1 ? value+"\n":value));

            try {
                writeValue(randomAccessFile, temp);
            } catch (IOException e) {
                return e.getMessage();
            }
            ServerConfig.INSTAND.restart();
            return "OK";
        });
    }

    private void writeValue(RandomAccessFile randomAccessFile, String temp) throws IOException {
        randomAccessFile.seek(0);
        //旧数据填充0
        for (int i = 0; i < randomAccessFile.length(); i++) {
            randomAccessFile.writeByte(0);
        }
        randomAccessFile.seek(0);
        randomAccessFile.writeBytes(temp);

        randomAccessFile.close();
    }

    private String getWriteValue(String[] v) {
        StringBuilder writeValue = new StringBuilder();
        for (String itemStr : v) {
            writeValue.append(itemStr);
        }
        return writeValue.toString();
    }


    private int filedParser(String field) {
        switch (field) {
            case "clientId":
                return 2;
            case "clientUrl":
                return 3;
            case "port":
                return 1;
            case "domain":
                return 0;
            default:
                return -1;
        }
    }

    private String[] valueParser(String value) {
        return value.split(",");
    }

    /*private String setCilentId(String clientId,String newClientId){
        return readLine((line, randomAccessFile,strings) -> {
            String temp =  line.replace(clientId, newClientId);
            try {
                randomAccessFile.seek(0);
                randomAccessFile.writeBytes(temp);

                //旧数据填充0
                for(int i = temp.getBytes().length; i < randomAccessFile.length(); i++){
                    randomAccessFile.writeByte(0);
                }

                randomAccessFile.close();
            } catch (IOException e) {
                //e.printStackTrace();
                return e.getMessage();
            }
            ServerConfig.INSTAND.restart();
            return "OK";
        });
    }*/


    private int getSeekValue(String clientId, String newClientId) {
        return Math.abs(clientId.getBytes().length - newClientId.getBytes().length) - 3;
    }


    private void getCli(List cli, String[] str) {
        for (String item : str) {
            if (!StringUtils.isEmpty(item)) {
                cli.add(item);
            }
        }
    }


    interface Call {
        String call(String value, RandomAccessFile randomAccessFile, List<String> strings);
    }


    @SneakyThrows
    private String readLine(Call call) {
        String path = PathUtils.getPath(ServerConfig.class);
        String configPath = path + File.separator + ConfigRead.CONFIG_FILE_NAME;
        RandomAccessFile randomAccessFile = new RandomAccessFile(configPath, "rw");
        String line;
        int index = 0;
        StringBuilder stringBuilder = new StringBuilder();
        List<String> strings = new LinkedList<>();
        while ((line = randomAccessFile.readLine()) != null) {
            String item = line + "\n";
            stringBuilder.append(item);
            strings.add(item);
        }
        return call.call(stringBuilder.toString(), randomAccessFile, strings);
    }
}




