package com.haojiangbo.parser.imp;

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.common.ColumnVauleType;
import com.haojiangbo.common.DBFile;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;
import com.haojiangbo.router.SQLRouter;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * 创建表 ddl 解析
* @Title: CreateStatementParser
* @Package com.haojiangbo.parser.imp
* @Description: (用一句话描述)
* @author 郝江波
* @date 2020/10/26
* @version V1.0
*/
public class CreateStatementParser extends CommonStatementParser implements StatementParserInteface<SQLCreateTableStatement,Boolean> {

    /**
     *  创建表 ddl
     * @param createTableStatement
     */
    @Override
    public  Boolean  parser(SQLCreateTableStatement createTableStatement) {
        // 表信息解析
        SQLTableSource sqlTableSource = createTableStatement.getTableSource();
        String dbName = sqlExprTableSourceHander(sqlTableSource);

        List<SQLTableElement> sqlTableElements = createTableStatement.getTableElementList();
        JSONObject tableHeader = new JSONObject(true);
        for (SQLTableElement element : sqlTableElements) {
            if (element instanceof SQLColumnDefinition) {
                SQLColumnDefinition definition = (SQLColumnDefinition) element;
                switch (definition.getDataType().getName().toUpperCase()) {
                    case SQLDataType.Constants.INT:
                        tableHeader.put(definition.getColumnName().replace("`", "").toUpperCase(), ColumnVauleType.INT.getValue());
                        break;
                    case SQLDataType.Constants.BIGINT:
                        tableHeader.put(definition.getColumnName().replace("`", "").toUpperCase(), ColumnVauleType.BIGINT.getValue());
                        break;
                    case SQLDataType.Constants.VARCHAR:
                        tableHeader.put(definition.getColumnName().replace("`", "").toUpperCase(), ColumnVauleType.VARCHAR.getValue());
                        break;
                    case SQLDataType.Constants.CHAR:
                        tableHeader.put(definition.getColumnName().replace("`", "").toUpperCase(), ColumnVauleType.VARCHAR.getValue());
                        break;
                    case SQLDataType.Constants.TINYINT:
                        tableHeader.put(definition.getColumnName().replace("`", "").toUpperCase(), ColumnVauleType.INT.getValue());
                        break;
                    case SQLDataType.Constants.DATE:
                        tableHeader.put(definition.getColumnName().replace("`", "").toUpperCase(), ColumnVauleType.DATE.getValue());
                        break;
                    default:
                        throw new RuntimeException("不支持的类型");
                }

            } else if (element instanceof MySqlPrimaryKey) {
                MySqlPrimaryKey primaryKey = (MySqlPrimaryKey) element;
                tableHeader.put("PRIMARY", ((SQLIdentifierExpr) primaryKey.getIndexDefinition().getColumns().get(0).getExpr()).getName().replace("`",""));
            }
        }
        try {
            File path = new File(SQLRouter.getDbPath());
            if (!path.exists()) {
                path.mkdirs();
            }
            File hti = new File(SQLRouter.getDbPath() + dbName + DBFile.HTI);
            if (hti.exists()) {
                //throw new RuntimeException("table 已存在");
                System.out.println("表已经存在");
                return false;
            }
            String headerInfo = tableHeader.toJSONString();
            RandomAccessFile tableInfo = new RandomAccessFile(hti, "rws");
            tableInfo.seek(0);
            tableInfo.writeInt(headerInfo.getBytes().length);
            tableInfo.write(headerInfo.getBytes());
            tableInfo.close();

            // table数据
            RandomAccessFile tableData
                    = new RandomAccessFile
                    (new File(SQLRouter.getDbPath() + dbName + DBFile.HT), "rws");
            tableData.seek(0);
            tableData.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("table Metadata == >" + tableHeader.toJSONString());
        return  true;
    }

    @Override
    protected LeftValueParserInteface getLeftValueParser() {
        return null;
    }
}
