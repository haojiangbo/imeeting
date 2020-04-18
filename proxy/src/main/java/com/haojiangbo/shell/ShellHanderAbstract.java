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
    public static final String GET      = "get";
    public static final String GETLIST  = "getlist";
    public static final String SET      = "set";




    public String distributor(String line){
        String prefix = line.split(" ")[0];
        switch (prefix){
            case HELP:
                return helpCmd();
            case FLUSH:
                return flush();
            case EXIT:
                return exit();
            case GET:
                return get(line.split(" "));
            case GETLIST:
                return getlist();
            case SET:
                return set(line.split(" "));
            default:
                return "不能识别的命令 请输入 help";
        }

    }


    private String helpCmd(){
        return "目前只支持1条命令 就是 flush 刷新服务端缓存";
    }


    protected abstract String flush();

    protected abstract String exit();

    protected abstract String get(String... str);

    protected abstract String getlist();


    protected abstract String set(String... str);

}
