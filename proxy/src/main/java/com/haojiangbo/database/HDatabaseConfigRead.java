package com.haojiangbo.database;
import com.haojiangbo.config.ClientCheckConfig;
import com.haojiangbo.config.ConfigRead;
import com.haojiangbo.datamodel.HDatabasseRowModel;
import com.haojiangbo.model.ConfigModel;
import com.haojiangbo.router.SQLRouter;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
* @Title: HDatabaseConfigRead
* @Package com.haojiangbo.database
* @Description: 数据库读取
* @author 郝江波
* @date 2020/10/28
* @version V1.0
*/
public class HDatabaseConfigRead implements ConfigRead {

    private static final String GET_CONFIG = "select * from config";
    private static final String CREATE_ROUTER_CONFIG = "create table config (domain varchar,port int,clientId varchar,clientUrl varchar)";

    @Override
    public List<ConfigModel> readLine(String path) {
        String  INSERT_VALUES = "insert into config values ('xxx',65534,'"+ UUID.randomUUID().toString().substring(0,5) +"','127.0.0.1:80')";
        SQLRouter.setDbPath(path);
        SQLRouter.router(CREATE_ROUTER_CONFIG);
        List<HDatabasseRowModel> result = (List<HDatabasseRowModel>) SQLRouter.router(GET_CONFIG);
        if(result == null || result.size() == 0){
            SQLRouter.router(INSERT_VALUES);
        }
        result = (List<HDatabasseRowModel>) SQLRouter.router(GET_CONFIG);
        return converDataToList(result);
    }

    /**
     *
     * @param result
     */
    private List<ConfigModel>  converDataToList(List<HDatabasseRowModel> result){
        List<ConfigModel> models = new LinkedList<>();
        for(HDatabasseRowModel row : result){
            Class clazz = ConfigModel.class;
            try {
                Object intance =  clazz.newInstance();
                Field [] fields = clazz.getDeclaredFields();
                for(Field f : fields){
                    f.setAccessible(true);
                    f.set(intance,row.getData().get(f.getName().toUpperCase()).getValue());
                }
                ConfigModel configModel =  (ConfigModel) intance;
                ClientCheckConfig.CLIENT_CHECK_MAP.put(configModel.getClientId(),configModel);
                models.add(configModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return  models;
    }



}
