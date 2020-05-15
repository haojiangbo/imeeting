package com.haojiangbo.shell;

import com.haojiangbo.config.ServerConfig;
import com.haojiangbo.container.EventClientEngineContainner;
import com.haojiangbo.inteface.Container;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 　　* @author 郝江波
 * 　　* @date 2020/4/18 11:05
 */
public class CmdShellHander {

    private static AbstractShellHander shellHander = new DefaultShellHanderImp();

    /**
     * 测试类
     *
     * @param a
     */
    public  void main(String[] a) {
        start(new ServerConfig(), new EventClientEngineContainner());
    }

    @SneakyThrows
    public static void start(Container... container) {
        for (Container item : container) {
            item.start();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        println("输入 help 查看帮助");
        print(">");
        while ((line = br.readLine()) != null) {
            if (!StringUtils.isEmpty(line)) {
                String result = shellHander.distributor(line);
                if (!result.equals(AbstractShellHander.EXIT)) {
                    System.out.println(result);
                } else {
                    br.close();
                    for (Container item : container) {
                        item.stop();
                    }
                    return;
                }
            }
            print(">");
        }
    }

    public static void print(String message) {
        System.out.print(message);
    }

    public static void println(String message) {
        System.out.println(message);
        print(">");
    }
}
