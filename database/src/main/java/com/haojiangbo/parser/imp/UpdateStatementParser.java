package com.haojiangbo.parser.imp;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.haojiangbo.datamodel.HDatabaseColumnModel;
import com.haojiangbo.datamodel.HDatabasseRowModel;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.option.imp.HDatabaseDruidASTLeftValueParser;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class UpdateStatementParser  extends CommonStatementParser
        implements StatementParserInteface<SQLUpdateStatement, Boolean> {



    @Override
    public Boolean parser(SQLUpdateStatement statement) {
        return  updateStatementParser(statement);
    }

    public boolean updateStatementParser(SQLUpdateStatement sqlStatement){
        List<SQLUpdateSetItem> sqlUpdateSetItems =  sqlStatement.getItems();
        if(null == sqlUpdateSetItems || sqlUpdateSetItems.size() == 0){
            throw new RuntimeException("update 语句错误 没有确切的set值");
        }
        String tableName = sqlExprTableSourceHander(sqlStatement.getTableSource());
        RandomAccessFile metaDataFile =  getTableMetaDataFile(tableName,"r");

        try {
            JSONObject metaData = JSONObject.parseObject(new String(getTableMetaDataArray(metaDataFile)), Feature.OrderedField);
            parserTableData(tableName,metaData,(tableDataFile,row) ->{
                SQLExpr sqlExpr = sqlStatement.getWhere();
                if(null == sqlExpr || ((Boolean) sqlWhereOptionExprParser(row,sqlExpr))){
                    row.setFlag(HDatabasseRowModel.DELETE);

                    try {
                        long markerPoint = tableDataFile.getFilePointer();
                        tableDataFile.seek(row.getOffset());
                        tableDataFile.write(row.toByteArray());

                        //设置值
                        for(SQLUpdateSetItem setItem  : sqlUpdateSetItems){
                            String colName = ((SQLIdentifierExpr) setItem.getColumn()).getName().toUpperCase();
                            SQLValuableExpr valuable = (SQLValuableExpr) setItem.getValue();
                            HDatabaseColumnModel columnModel =  row.getData().get(colName);
                            // 设置新value
                            java2byte(valuable.getValue(),columnModel);
                        }

                        // 移动到末尾 添加新数据
                        tableDataFile.seek(tableDataFile.length());
                        row.setOffset(tableDataFile.length());
                        row.setVersion(row.getVersion()+1);
                        row.setFlag(HDatabasseRowModel.ENABLE);
                        tableDataFile.write(row.toByteArray());

                        // 移动到原来的位置
                        tableDataFile.seek(markerPoint);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                metaDataFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  true;
    }


    @Override
    protected LeftValueParserInteface getLeftValueParser() {
        return new HDatabaseDruidASTLeftValueParser();
    }

}
