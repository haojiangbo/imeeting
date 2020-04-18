package com.haojiangbo.shell;
 /**
  * shell处理器
 　　* @author 郝江波
 　　* @date 2020/4/18 11:17
 　　*/
public abstract class ShellHanderAbstract {

    public static final String HELP     = "help";
    public static final String FLUSH    = "flush";
    public static final String EXIT     = "exit";

    public String distributor(String line){
        if(line.equals(HELP)){
            return helpCmd();
        }else if(line.equals(FLUSH)){
            return flush();
        }else if(line.equals(EXIT)){
            return exit();
        }else{
            return "不能识别的命令 请输入 help";
        }
    }


    private String helpCmd(){
        return "目前只支持1条命令 就是 flush 刷新服务端缓存";
    }


    protected abstract String flush();

     protected abstract String exit();
}
