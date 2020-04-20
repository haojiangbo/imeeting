package com.haojiangbo.utils;
/**
* @Title: PathUtils
* @Package com.haojiangbo.utils
* @Description: 获取当前文件目录
* @author 好江波
* @date 2020/4/16
* @version V1.0
*/
public class PathUtils {

    public static String getPath(Class clazz)
    {
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        if(System.getProperty("os.name").contains("dows"))
        {
            path = path.substring(1);
        }
        if(path.contains("jar"))
        {
            path = path.substring(0,path.lastIndexOf("."));
            return path.substring(0,path.lastIndexOf("/"));
        }
        return path.replace("target/classes/", "");
    }
}
