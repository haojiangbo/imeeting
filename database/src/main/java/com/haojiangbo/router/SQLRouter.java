package com.haojiangbo.router;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExplainStatement;
import com.haojiangbo.parser.imp.*;
import org.apache.commons.lang3.StringUtils;

import javax.xml.crypto.Data;
import java.io.File;
import java.util.List;

import static com.alibaba.druid.util.JdbcConstants.MYSQL;
/**
* @Title: SQLRouter
* @Package com.haojiangbo.router
* @Description: SQL路由
* @author 郝江波
* @date 2020/10/28
* @version V1.0
*/
public class SQLRouter {

    private static  String DB_PATH =  null;

    public static void setDbPath(String dbPath) {
        DB_PATH = dbPath+ File.separator;
    }

    public static String getDbPath() {
        if(StringUtils.isEmpty(DB_PATH)){
            throw new RuntimeException("DB_PATH 不能为空");
        }
        return DB_PATH;
    }

    public static Object router(String sql){
        if(StringUtils.isEmpty(DB_PATH)){
            throw new RuntimeException("DB_PATH 不能为空");
        }
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, MYSQL);
        return do_router(sqlStatements);
    }

    private static Object do_router(List<SQLStatement> sqlStatements) {
        SQLStatement sqlStatement = sqlStatements.get(0);
        if (sqlStatement instanceof SQLSelectStatement) {
          return  new SelectStatementParser().parser((SQLSelectStatement) sqlStatement);
        }else if (sqlStatement instanceof SQLCreateTableStatement) {
            CreateStatementParser createStatementParser = new CreateStatementParser();
            boolean b =    createStatementParser.parser((SQLCreateTableStatement) sqlStatement);
            if(b){
                System.out.println("创建成功");
            }else{
                return false;
            }
        }else if(sqlStatement instanceof SQLInsertStatement){
            boolean b =    new InsertStatmentParser().parser((SQLInsertStatement) sqlStatement);
            if(b){
                System.out.println("插入成功");
            }else{
                return false;
            }
        }else if(sqlStatement instanceof SQLUpdateStatement){
            boolean b =    new UpdateStatementParser().parser((SQLUpdateStatement) sqlStatement);
            if(b){
                System.out.println("修改成功");
            }
        }else if(sqlStatement instanceof SQLDeleteStatement){
            boolean b =    new DeleteStatementParser().parser((SQLDeleteStatement) sqlStatement);
            if(b){
                System.out.println("删除成功");
            }
        }else if(sqlStatement instanceof MySqlExplainStatement){
            new ExplainStatementParser().parser((MySqlExplainStatement) sqlStatement);
        }
        return true;
    }
}
