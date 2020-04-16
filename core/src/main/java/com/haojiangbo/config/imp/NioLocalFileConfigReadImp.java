package com.haojiangbo.config.imp;

import com.haojiangbo.config.ConfigRead;
import com.haojiangbo.model.ConfigModel;
import com.haojiangbo.utils.PathUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
@Slf4j
public class NioLocalFileConfigReadImp implements ConfigRead {

    @SneakyThrows
    @Override
    public List<ConfigModel> readLine(String path) {
        List<ConfigModel> result = new LinkedList<>();
        RandomAccessFile randomAccessFile = new RandomAccessFile(path+ConfigRead.CONFIG_FILE_NAME,"r");
        String line;
        for(line = randomAccessFile.readLine(); line != null; line = randomAccessFile.readLine()){
            if(line.startsWith("#"))
                continue;
            else
                result.add(parserLine(line));
        }
        return result;
    }

   private  ConfigModel parserLine(String line){
        ConfigModel configModel = new ConfigModel();
        String [] temp =  line.split(",");
        if(temp.length != 4){
            throw  new RuntimeException("配置参数错误,缺少参数，请参照注释");
        }
       configModel.setDomain(temp[0]);
       configModel.setPort(Integer.valueOf(temp[1]));
       configModel.setClientId(Integer.valueOf(temp[2]));
       configModel.setClientUrl(temp[3]);
       String [] hp = configModel.getClientUrl().split(":");
       if(hp.length != 2){
           throw  new RuntimeException("配置参数错误,错误的主机地址，请参照注释");
       }
       return configModel;
    }


}
