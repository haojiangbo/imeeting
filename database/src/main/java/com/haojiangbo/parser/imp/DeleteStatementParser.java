package com.haojiangbo.parser.imp;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.haojiangbo.datamodel.HDatabasseRowModel;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.option.imp.HDatabaseDruidASTLeftValueParser;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * 删除操作的处理
  * @author 郝江波
  * @date 2020/10/28 11:29
  */
public class DeleteStatementParser extends CommonStatementParser
       implements StatementParserInteface<SQLDeleteStatement, Boolean> {



   @Override
   public Boolean parser(SQLDeleteStatement statement) {
       return  deleteStatementParser(statement);
   }

   public boolean deleteStatementParser(SQLDeleteStatement sqlStatement){

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
