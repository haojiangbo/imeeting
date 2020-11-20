package com.haojiangbo.storage;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

/**
 * 存储注册的电话号码
 */
public class NumberStorageManager {
    private static String GLOBAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/callme/";

    private static class NumberStorageManagerHold {
        final static NumberStorageManager instand = new NumberStorageManager();
    }

    public static NumberStorageManager build() {
        return NumberStorageManagerHold.instand;
    }

    public NumberStorageManager() {
        File file = new File(GLOBAL_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public  void putData(String fileName, byte[] data) {
        File file = new File(GLOBAL_PATH + fileName);
        if(file.exists()){
            file.delete();
        }
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  byte[] getData(String fileName) {
        File file = new File(GLOBAL_PATH + fileName);
        if (!file.exists()) {
            return null;
        }
        int totalLen = 1024;
        byte[] data = new byte[totalLen];
        ByteBuf byteBuf = new ByteBuf();
        try (InputStream inputStream = new FileInputStream(file)) {
            int len = 0;
            while ((len = inputStream.read(data)) != -1) {
                byteBuf.write(data,0,len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteBuf.toByteArray();
    }

    /**
     * byteBuf工具类
     * 用到哪实现到哪吧
     */
    private static class ByteBuf {
        int readIndex = 0;
        int writeIndex = 0;
        int size = 3;
        byte[] data = new byte[size];

        public void write(byte[] b,int startIndex, int endIndex) {
            if ((size - writeIndex) < (endIndex - startIndex)) {
                int totalSize = size + ((endIndex - startIndex) - (size - writeIndex));
                byte[] tmp = new byte[totalSize];
                System.arraycopy(data, 0, tmp, 0, size);
                size = totalSize;
                data = tmp;
            }
            System.arraycopy(b, startIndex, data, writeIndex, (endIndex - startIndex));
            writeIndex += (endIndex - startIndex);
        }

        public byte[] toByteArray(){
            if(writeIndex != 0){
                byte [] r = new byte[writeIndex];
                System.arraycopy(data,0,r,0,writeIndex);
                return r;
            }else {
                return null;
            }
        }

    }


}
