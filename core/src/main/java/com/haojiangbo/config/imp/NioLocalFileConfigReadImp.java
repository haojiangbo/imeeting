package com.haojiangbo.config.imp;

import com.haojiangbo.config.ClientCheckConfig;
import com.haojiangbo.config.ConfigRead;
import com.haojiangbo.model.ConfigModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
 /**
  *
  * 本地配置文件读取
 　　* @author 郝江波
 　　* @date 2020/4/17 10:41
 　　*/
@Slf4j
public class NioLocalFileConfigReadImp implements ConfigRead {

    @SneakyThrows
    @Override
    public List<ConfigModel> readLine(String path) {
        log.info("准备从 {} 下加载 配置文件", path);
        List<ConfigModel> result = new LinkedList<>();
        String configPath = path+File.separator+ConfigRead.CONFIG_FILE_NAME;
        RandomAccessFile randomAccessFile = new RandomAccessFile(configPath,"r");
        log.info("配置文件加载成功 {} ", configPath);
        String line;
        for(line = randomAccessFile.readLine(); line != null; line = randomAccessFile.readLine()){
            line = line.trim();
            if(!StringUtils.isEmpty(line)){
                if(line.startsWith("#")) {
                    continue;
                } else {
                    result.add(parserLine(line));
                }
            }
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
       configModel.setClientId(temp[2]);
       configModel.setClientUrl(temp[3]);
       String [] hp = configModel.getClientUrl().split(":");
       if(hp.length != 2){
           throw  new RuntimeException("配置参数错误,错误的主机地址，请参照注释");
       }
       ClientCheckConfig.CLIENT_CHECK_MAP.put(temp[2],configModel);
       return configModel;
    }


}
