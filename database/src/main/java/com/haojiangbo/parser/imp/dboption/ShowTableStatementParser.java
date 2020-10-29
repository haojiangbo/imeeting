package com.haojiangbo.parser.imp.dboption;

import com.alibaba.druid.sql.ast.statement.SQLShowTablesStatement;
import com.haojiangbo.common.DBFile;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;
import com.haojiangbo.router.SQLRouter;
import java.io.File;

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
        for(File f : childList){
           if(f.getName().endsWith(DBFile.HTI)){
               System.out.println(String.format("| %-15s |",f.getName().substring(0,f.getName().length()-4)));
           }
        }
        System.out.println(genLineString(1));
        return  true;
    }
}
