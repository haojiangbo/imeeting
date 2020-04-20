package com.haojiangbo.shell;

import com.sun.org.apache.regexp.internal.RE;

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
    public static final String LIST     = "list";
    public static final String SET      = "set";
    public static final String DEL      = "del";
    public static final String ADD      = "add";




    public String distributor(String line){
        String [] tokens = line.split(" ");
        String prefix = tokens[0];
        switch (prefix){
            case HELP:
                return helpCmd();
            case FLUSH:
                return flush();
            case EXIT:
                return exit();
            case GET:
                return get(tokens);
            case LIST:
                return getlist();
            case SET:
                return set(tokens);
            case ADD:
                return add(tokens);
            case DEL:
                return del(tokens);
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

    protected abstract String add(String... str);

    protected abstract String del(String... str);

}
