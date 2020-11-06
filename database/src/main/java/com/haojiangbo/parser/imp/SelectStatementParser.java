package com.haojiangbo.parser.imp;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOrderingSpecification;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.haojiangbo.datamodel.HDatabaseColumnModel;
import com.haojiangbo.datamodel.HDatabaseTableModel;
import com.haojiangbo.datamodel.HDatabasseRowModel;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.option.imp.HDatabaseDruidASTLeftValueParser;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.ResultSetPacket;
import com.haojiangbo.thread.RuntimeInstance;
import com.haojiangbo.utils.MetaDataUtils;

import java.io.RandomAccessFile;
import java.util.*;

/**
 * @author 郝江波
 * @version V1.0
 * @Title: SelectStatementParser
 * @Package com.haojiangbo.parser.imp
 * @Description: 查询解析
 * @date 2020/10/26
 */
public class SelectStatementParser
        extends CommonStatementParser
        implements StatementParserInteface<SQLSelectStatement, List> {

    /**
     * 查询 Statement
     *
     * @param sqlStatement
     * @return
     */
    @Override
    public List parser(SQLSelectStatement sqlStatement) {
        SQLSelectStatement selectStatement = sqlStatement;
        SQLSelect sqlSelect = selectStatement.getSelect();
        // 这个query 才是真正干活的 此处只兼容 mysql
        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlSelect.getQuery();

        // 表信息解析
        SQLTableSource sqlTableSource = query.getFrom();
        String tableName = sqlExprTableSourceHander(sqlTableSource);

        // 结果集
        List<HDatabasseRowModel> tableList;
        if (query.getOrderBy() != null) {
            // 提高检索速度 对排序提速
            tableList = new ArrayList<>();
        } else {
            tableList = new LinkedList<>();
        }
        long startTime = System.currentTimeMillis();
        try {
            RandomAccessFile tableInfo = getTableMetaDataFile(tableName, "r");
            if (tableInfo == null) {
                System.out.println("表不存在");
                return null;
            }
            byte[] data = getTableMetaDataArray(tableInfo);
            tableInfo.close();

            JSONObject tableMetaData = JSONObject.parseObject(new String(data), Feature.OrderedField);
            //System.out.println("table metadata => " + tableMetaData.toJSONString());

            // 解析数据
            parserTableData(tableName, tableMetaData, (tableData, hDatabasseRowModel) -> {
                // where语句解析  &&  where 条件执行
                SQLExpr sqlExpr = query.getWhere();
                if (null != sqlExpr) {
                    Boolean result = (Boolean) sqlWhereOptionExprParser(hDatabasseRowModel, query.getWhere());
                    // 结果集
                    //System.out.println("where exprParser result => " + result);
                    // 此处放入到list只是为了方便使用
                    // 如果数据量过大，可能会导致内存溢出
                    // 正常逻辑来说，存入buff或者写入临时文件 sort or group 操作
                    // 再通过网络发送到 dbClient
                    if (result) {
                        tableList.add(hDatabasseRowModel);
                    }
                } else {
                    tableList.add(hDatabasseRowModel);
                }
            });

            orderByHander(tableList, query);

            BaseMysqlPacket packet =  RuntimeInstance.currentThreadPacket.get();
            ResultSetPacket resultSetPacket = new ResultSetPacket();

            resultSetPacket.buildHeader(tableMetaData,HDatabaseTableModel.DEFULT_DATABASE_NAME,tableName);

            int maxTitleLength = 0;
            List<String []> rowData = new LinkedList<>();
            for (int i = 0; i < tableList.size(); i++) {
                Iterator<Map.Entry<String, HDatabaseColumnModel>> colIterator = tableList.get(i).getData().entrySet().iterator();
                if (i == 0) {
                    StringBuilder tmp = new StringBuilder();
                    tmp.append("|");
                    for (Map.Entry<String, Object> meta : tableMetaData.entrySet()) {
                        if (meta.getKey().equals(HDatabaseTableModel.PRIMARY)) {
                            continue;
                        }
                        maxTitleLength++;
                        tmp.append(String.format(" %-15s|", meta.getKey()));
                    }
                    System.out.println(genLineString(maxTitleLength));
                    System.out.println(tmp);
                    System.out.println(genLineString(maxTitleLength));
                }
                StringBuilder tmp = new StringBuilder();
                tmp.append("|");
                String [] colDataArray = new String[MetaDataUtils.calcMetaDataSize(tableMetaData)];
                int index = 0;
                for (Map.Entry<String, Object> meta : tableMetaData.entrySet()) {
                    if (meta.getKey().equals(HDatabaseTableModel.PRIMARY)) {
                        continue;
                    }
                    Map.Entry<String, HDatabaseColumnModel> colData = colIterator.next();
                    String value = colData.getValue().getSize() == 0 ? "NULL" : colData.getValue().getValue().toString();
                    colDataArray[index] = value;
                    tmp.append(String.format(" %-15s|", value));
                    index ++;
                }
                rowData.add(colDataArray);
                System.out.println(tmp);
            }
            resultSetPacket.buildRowData(rowData);
            if(null != packet){
                resultSetPacket.write(packet);
            }
            System.out.println(genLineString(maxTitleLength));
            //System.out.println(JSONArray.toJSONString(tableList,true));
            System.out.println("共 " + tableList.size() + " 条,耗时 " + (System.currentTimeMillis() - startTime) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableList;
    }



    /**
     * 排序处理
     *
     * @param tableList
     * @param query
     */
    private void orderByHander(List<HDatabasseRowModel> tableList, MySqlSelectQueryBlock query) {
        SQLOrderBy orderBy = query.getOrderBy();
        if (null != orderBy) {
            List<SQLSelectOrderByItem> orderByItems = orderBy.getItems();
            if (null == orderByItems) {
                return;
            }
            int totalCount = tableList.size();
            if (totalCount > 10000 * 1) {
                throw new RuntimeException("数据已超过1万条,无法直接在内存排序");
            }
            for (SQLSelectOrderByItem orderItem : orderByItems) {
                String fieldKey = ((SQLIdentifierExpr) orderItem.getExpr()).getName().replace("`","").toUpperCase();
                sortResutListHander(fieldKey,tableList,0,tableList.size()-1);
                if(orderItem.getType() == SQLOrderingSpecification.DESC){
                    Collections.reverse(tableList);
                }
            }
        }

    }

    /**
     * 快速排序
     * @param fieldKey
     * @param tableList
     */
    private void sortResutListHander(String fieldKey, List<HDatabasseRowModel> tableList, int start, int end) {

        int i = start;
        int j = end;
        HDatabasseRowModel p = tableList.get(i);
        if(!p.getData().containsKey(fieldKey)){
            throw  new RuntimeException(" "+fieldKey+" 列不存在");
        }
        while (i < j){
            Object tmpData = p.getData().get(fieldKey).getValue();
            if (tmpData == null || tmpData.toString().length() == 0) {
                p = tableList.get(i);
                i++;
            }else {
                break;
            }
        }
        // 整列都是 NULL 那就干脆不要排序好了
        if(i == j){
            return;
        }

        while (i < j) {
            while (i < j) {
                HDatabasseRowModel row = tableList.get(j);
                Map<String, HDatabaseColumnModel> col = row.getData();
                Object data = col.get(fieldKey).getValue();
                if (data instanceof Comparable) {
                    if (((Comparable) data).compareTo(p.getData().get(fieldKey).getValue()) <= 0) {
                        tableList.set(i, row);
                        i++;
                        break;
                    }
                    j--;
                } else if (data instanceof String) {
                    if (data.toString().charAt(0) <= p.getData().get(fieldKey).getValue().toString().charAt(0)) {
                        tableList.set(i, row);
                        i++;
                        break;
                    }
                    j--;
                } else {
                    break;
                }
            }
            while (i < j) {
                HDatabasseRowModel row = tableList.get(i);
                Map<String, HDatabaseColumnModel> col = row.getData();
                Object data = col.get(fieldKey).getValue();
                if(data == null){
                    tableList.set(j, row);
                    break;
                }
                if (data instanceof Comparable) {
                    if (((Comparable) data).compareTo(p.getData().get(fieldKey).getValue()) >= 0) {
                        tableList.set(j, row);
                        break;
                    }
                    i++;
                } else if (data instanceof String) {
                    if ((data.toString().length() == 0) || data.toString().charAt(0) >= p.getData().get(fieldKey).getValue().toString().charAt(0)) {
                        tableList.set(j, row);
                        break;
                    }
                    i++;
                } else {
                    throw new RuntimeException("不支持的排序类型");
                }
            }
        }

        if (i == j) {
            tableList.set(i, p);
        }

        if (start < i) {
            sortResutListHander(fieldKey, tableList, 0, i - 1);
        }
        if (end > j) {
            sortResutListHander(fieldKey, tableList, j + 1, end);
        }
    }




    @Override
    protected LeftValueParserInteface getLeftValueParser() {
        return new HDatabaseDruidASTLeftValueParser();
    }
}
