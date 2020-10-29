package com.haojiangbo.parser.imp;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.haojiangbo.common.ColumnVauleType;
import com.haojiangbo.datamodel.HDatabaseColumnModel;
import com.haojiangbo.datamodel.HDatabaseTableModel;
import com.haojiangbo.datamodel.HDatabasseRowModel;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;
import com.haojiangbo.utils.ByteUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
* @Title: InsertStatmentParser
* @Package com.haojiangbo.parser.imp
* @Description: insert语句解析类
* @author 郝江波
* @date 2020/10/27
* @version V1.0
*/
public class InsertStatmentParser extends CommonStatementParser
        implements StatementParserInteface<SQLInsertStatement, Boolean> {

    @Override
    public Boolean parser(SQLInsertStatement statement) {
        try {
            return insertStatementHander(statement);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }



    private boolean insertStatementHander(SQLInsertStatement sqlInsertStatement) throws IOException {
        // 表信息解析
        SQLTableSource sqlTableSource = sqlInsertStatement.getTableSource();
        String tableName = sqlExprTableSourceHander(sqlTableSource);
        RandomAccessFile tableFile =  getTableMetaDataFile(tableName,"rws");

        List<SQLExpr> columns =  sqlInsertStatement.getColumns();
        List<SQLExpr> values = sqlInsertStatement.getValuesList().get(0).getValues();


        JSONObject insertColumns = null;
        byte [] data =   getTableMetaDataArray(tableFile);
        JSONObject baseColumnsInfo =   JSONObject.parseObject(new String(data), Feature.OrderedField);
        tableFile.close();

        if(null != columns && columns.size() > 0){
            if(columns.size() != values.size()){
                throw  new RuntimeException("sql错误 插入的 column 为 "+columns.size() +"; value 为 " + values.size());
            }
            insertColumns = new JSONObject(true);
            int i = 0;
            for(SQLExpr item : columns){
                String key = ((SQLIdentifierExpr)item).getName().toUpperCase();
                if(!baseColumnsInfo.containsKey(key)){
                    throw  new RuntimeException("不存在的列");
                }
                insertColumns.put(key,values.get(i));
                i++;
            }
        }

        HDatabasseRowModel hDatabasseRowModel = new HDatabasseRowModel();
        List<HDatabaseColumnModel>  waitSaveColumn = new LinkedList<>();

        if(insertColumns == null){
            autoInsertHander(baseColumnsInfo, values, waitSaveColumn);
        }else {
            // 指定要插入的字段
            notAutoInsertHander(values, insertColumns, baseColumnsInfo, waitSaveColumn);
        }
        RandomAccessFile tableDataFile =  getTableDataFile(tableName,"rws");
        try {
            tableDataFile.seek(tableDataFile.length());
            tableDataFile.write(hDatabasseRowModel.
                    toByteArray(waitSaveColumn,tableDataFile.getFilePointer(),
                            HDatabasseRowModel.ENABLE,System.currentTimeMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            tableDataFile.close();
        }

        return true;
    }

    private void notAutoInsertHander(List<SQLExpr> values, JSONObject insertColumns, JSONObject baseColumnsInfo, List<HDatabaseColumnModel> waitSaveColumn) {
        int i = 0;
        for(Map.Entry<String, Object> entry : baseColumnsInfo.entrySet()){
            if(entry.getKey().equals(HDatabaseTableModel.PRIMARY)){
                continue;
            }
            HDatabaseColumnModel columnModel = new HDatabaseColumnModel();
            if(insertColumns.containsKey(entry.getKey())){
                columnModel.setType(baseColumnsInfo.getByte(entry.getKey()).byteValue());
                java2byte(((SQLValuableExpr)values.get(i)).getValue(), columnModel);
                waitSaveColumn.add(columnModel);
                i++;
            }else{
                columnModel.setType(baseColumnsInfo.getByte(entry.getKey()).byteValue());
                columnModel.setData(new byte[0]);
                waitSaveColumn.add(columnModel);
            }
        }
    }

    private void autoInsertHander(JSONObject baseColumnsInfo, List<SQLExpr> values, List<HDatabaseColumnModel> waitSaveColumn) {
        HDatabaseColumnModel columnModel;
        if(baseColumnsInfo.size()-values.size() > 2){
            throw  new RuntimeException("传入的参数有错误，缺少列");
        }
        Map.Entry<String, Object> pri = null;
        for(Map.Entry<String, Object> entry : baseColumnsInfo.entrySet()) {
            if (entry.getKey().equals(HDatabaseTableModel.PRIMARY)) {
                pri = entry;
                break;
            }
        }
        // 删除主键原数据
        if(null != pri){
            baseColumnsInfo.remove(HDatabaseTableModel.PRIMARY);
        }

        // 说明没有带着ID 需要自己生成ID
        if(baseColumnsInfo.size() > values.size()){

            int i = -1;
            for(Map.Entry<String, Object> entry : baseColumnsInfo.entrySet()){
                // 处理自动主键  只能在建表的第一个字段 否则无法处理 自动赋值的情况
                columnModel = new HDatabaseColumnModel();
                if(i == -1){
                    columnModel.setType(ColumnVauleType.BIGINT.getValue());
                    columnModel.setData(ByteUtil.getBytes(System.currentTimeMillis()));
                    waitSaveColumn.add(columnModel);
                    i++;
                    continue;
                }
                columnModel.setType(baseColumnsInfo.getByte(entry.getKey()).byteValue());
                java2byte(((SQLValuableExpr)values.get(i)).getValue(), columnModel);
                waitSaveColumn.add(columnModel);
                i++;
            }
        }else{
            int i = 0;
            for(Map.Entry<String, Object> entry : baseColumnsInfo.entrySet()){
                columnModel = new HDatabaseColumnModel();
                columnModel.setType(baseColumnsInfo.getByte(entry.getKey()).byteValue());
                java2byte(((SQLValuableExpr)values.get(i)).getValue(), columnModel);
                waitSaveColumn.add(columnModel);
                i++;
            }
        }
    }



    @Override
    protected LeftValueParserInteface getLeftValueParser() {
        return null;
    }
}
