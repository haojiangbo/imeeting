package com.haojiangbo.parser.imp.dboption;

import com.alibaba.druid.sql.ast.statement.SQLShowTablesStatement;
import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.common.ColumnVauleType;
import com.haojiangbo.common.DBFile;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.ResultSetPacket;
import com.haojiangbo.router.SQLRouter;
import com.haojiangbo.thread.RuntimeInstance;
import com.haojiangbo.utils.MetaDataUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ShowTableStatementParser extends CommonStatementParser
        implements StatementParserInteface<SQLShowTablesStatement,Boolean> {
    @Override
    protected LeftValueParserInteface getLeftValueParser() {
        return null;
    }

    @Override
    public Boolean parser(SQLShowTablesStatement statement) {
        String dbPath =  SQLRouter.getDbPath();
        File file = new File(dbPath);
        File[] childList =  file.listFiles();
        System.out.println(genLineString(1));
        System.out.println(String.format("| %-15s |","TABLE_NAME"));
        System.out.println(genLineString(1));
        JSONObject metaData = new JSONObject(true);
        metaData.put("TABLE_SCHEMA", ColumnVauleType.VARCHAR.getValue());
        metaData.put("TABLE_NAME", ColumnVauleType.VARCHAR.getValue());
        metaData.put("TABLE_TYPE", ColumnVauleType.VARCHAR.getValue());
        BaseMysqlPacket packet =  RuntimeInstance.currentThreadPacket.get();
        ResultSetPacket resultSetPacket = new ResultSetPacket();
        //检查是不是网络包
        if(null != packet){
            resultSetPacket.buildHeader(metaData,"information_schema","TABLES");
        }
        int columeSize =  MetaDataUtils.calcMetaDataSize(metaData);
        for(File f : childList){
            List<String []> data = new LinkedList<>();
            if(f.getName().endsWith(DBFile.HTI)){
                String name = f.getName().substring(0,f.getName().length()-4);
                System.out.println(String.format("| %-15s |",name));
                String [] value = new String[columeSize];
                value[0] = name;
                value[1] = "branch_table";
                value[2] = "BASE TABLE";
                data.add(value);
            }
            resultSetPacket.buildRowData(data);
        }

        resultSetPacket.write(packet);
        System.out.println(genLineString(1));

        return  true;
    }


}
