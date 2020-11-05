package com.haojiangbo.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.common.ColumnVauleType;
import com.haojiangbo.common.DBFile;
import com.haojiangbo.datamodel.HDatabaseColumnModel;
import com.haojiangbo.datamodel.HDatabasseRowModel;
import com.haojiangbo.inteface.TableDataParserCallback;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.router.SQLRouter;
import com.haojiangbo.utils.ByteUtil;
import com.haojiangbo.utils.DateUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author 郝江波
 * @version V1.0
 * @Title: CommonStatementParser
 * @Package com.haojiangbo.parser
 * @Description: 公共解析模板方法
 * @date 2020/10/26
 */
public abstract class CommonStatementParser {


    protected abstract LeftValueParserInteface getLeftValueParser();


    /**
     * @param sqlTableSource
     */
    public String sqlExprTableSourceHander(SQLTableSource sqlTableSource) {
        String name = null;
        if (sqlTableSource instanceof SQLExprTableSource) {
            name = ((SQLExprTableSource) sqlTableSource).getName().getSimpleName().replace("`", "").toUpperCase();
            //System.out.println("tableName = > " + name);
        }
        return name;
    }

    /**
     * 得到表file
     * @param tableName
     * @param model
     * @return
     */
    public RandomAccessFile getTableMetaDataFile(String tableName, String model) {
        try {
            return new RandomAccessFile(new File(SQLRouter.getDbPath() + tableName + DBFile.HTI), model);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    public RandomAccessFile getTableDataFile(String tableName, String model) {
        try {
            return new RandomAccessFile(new File(SQLRouter.getDbPath() + tableName + DBFile.HT), model);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 得到表头数据
     * @param tableInfo
     * @return
     * @throws IOException
     */
    protected byte[] getTableMetaDataArray(RandomAccessFile tableInfo) {
        byte[] data = null;
        try {
            tableInfo.seek(0);
            int headerSize = tableInfo.readInt();
            data = new byte[headerSize];
            tableInfo.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }



    /**
     * where操作解析
     * @param sqlExpr
     * @return
     */
    protected Object sqlWhereOptionExprParser(HDatabasseRowModel row, SQLExpr sqlExpr) {
        // 如果是二分操作类
        if (sqlExpr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) sqlExpr;
            Object left = sqlWhereOptionExprParser(row,sqlBinaryOpExpr.getLeft());
            Object rigth = sqlWhereOptionExprParser(row,sqlBinaryOpExpr.getRight());
            SQLBinaryOperator sqlBinaryOperator = sqlBinaryOpExpr.getOperator();
            // System.out.println(left + " " + sqlBinaryOperator.getName() + " " + rigth);
            return optionTypeHander(row,left, rigth, sqlBinaryOperator);
        }
        // 如果是变量定义
        else if (sqlExpr instanceof SQLIdentifierExpr) {
            SQLIdentifierExpr sqlIdentifierExpr = (SQLIdentifierExpr) sqlExpr;
            return sqlIdentifierExpr;
        }
        // 别名定义
        else if (sqlExpr instanceof SQLPropertyExpr) {
            SQLPropertyExpr sqlPropertyExpr = (SQLPropertyExpr) sqlExpr;
            return sqlPropertyExpr;
        }
        // 值定义
        else if (sqlExpr instanceof SQLValuableExpr) {
            return ((SQLValuableExpr) sqlExpr).getValue();
        }
        throw new RuntimeException("未知的操作类型");
    }

    protected boolean optionTypeHander(HDatabasseRowModel row, Object left, Object rigth, SQLBinaryOperator operator) {
        // 如果是四则运算
        if (operator.isArithmetic()) {
            switch (operator) {
                case Add:
                    return true;
                // 减
                case Subtract:
                    return true;
                //乘
                case Multiply:
                    return true;
                //除
                case Divide:
                    return true;
                case DIV:
                    return true;
                case Modulus:
                    return true;
                case Mod:
                    return true;
                default:
                    return true;
            }
        }
        // 逻辑运算
        else if (operator.isLogical()) {
            // BooleanAnd || this == BooleanOr || this == BooleanXor
            switch (operator) {
                // 与
                case BooleanAnd:
                    return left.equals(true) && rigth.equals(true);
                // 或
                case BooleanOr:
                    return left.equals(true) || rigth.equals(true);
                // 异或
                case BooleanXor:
                    return !(left.equals(true) && rigth.equals(true));
                default:
                    throw new RuntimeException("不支持的操作数");
            }
        }
        // 关系运算
        else if (operator.isRelational()) {
            LeftValueParserInteface parser = (LeftValueParserInteface) getLeftValueParser().parserData(((SQLIdentifierExpr) left).getName().replace("`","").toUpperCase(),row);
            switch (operator) {
                case Equality:
                    return parser.equality(rigth);
                case Like:
                    return parser.Like(rigth);
                case SoudsLike:
                    return parser.SoudsLike(rigth);
                case NotEqual:
                    return parser.NotEqual(rigth);
                case GreaterThan:
                    return parser.GreaterThan(rigth);
                case GreaterThanOrEqual:
                    return parser.GreaterThanOrEqual(rigth);
                case LessThan:
                    return parser.LessThan(rigth);
                case LessThanOrEqual:
                    return parser.LessThanOrEqual(rigth);
                case LessThanOrGreater:
                    return parser.LessThanOrGreater(rigth);
                case NotLike:
                    return parser.NotLike(rigth);
                case NotLessThan:
                    return parser.NotLessThan(rigth);
                case NotGreaterThan:
                    return parser.NotGreaterThan(rigth);
                case RLike:
                    return parser.RLike(rigth);
                case NotRLike:
                    return parser.NotRLike(rigth);
                case RegExp:
                    return parser.RegExp(rigth);
                case NotRegExp:
                    return parser.NotRegExp(rigth);
                case Is:
                    return parser.Is(rigth);
                case IsNot:
                    return parser.IsNot(rigth);
            }
        }
        throw new RuntimeException("未知的操作数");
    }


    /**
     * 解析表数据
     * @param tableName
     * @param tableMetaData
     * @param tableDataParserCallback
     */
    protected void parserTableData(String tableName, JSONObject tableMetaData,
                                 TableDataParserCallback<RandomAccessFile, HDatabasseRowModel> tableDataParserCallback) {
        RandomAccessFile tableData = getTableDataFile(tableName, "rws");
        if(null == tableData){
            throw  new RuntimeException("表不存在");
        }
        try {
            long tableLength = tableData.length();
            while (tableData.getFilePointer() < tableLength){
                // 判断数据是否处于被删除状态
                byte flag = tableData.readByte();
                if(flag == HDatabasseRowModel.ENABLE){
                    HDatabasseRowModel hDatabasseRowModel  = new HDatabasseRowModel();

                    long offset = tableData.readLong();
                    long version = tableData.readLong();
                    int dataSize = tableData.readInt();

                    ByteBuffer columnBuff = ByteBuffer.allocate(dataSize);
                    tableData.getChannel().read(columnBuff);
                    columnBuff.flip();

                    hDatabasseRowModel.setOffset(offset);
                    hDatabasseRowModel.setFlag(flag);
                    hDatabasseRowModel.setSize(dataSize);
                    hDatabasseRowModel.setVersion(version);
                    hDatabasseRowModel.setData(new LinkedHashMap<>());

                    Iterator<String> iterator = tableMetaData.keySet().iterator();
                    while (columnBuff.remaining() > 0){
                        // 生成column数据
                        HDatabaseColumnModel columnModel = gethDatabaseColumnModel(columnBuff, iterator);
                        // 封装结果集
                        hDatabasseRowModel.getData().put(columnModel.getName(),columnModel);
                    }
                    columnBuff = null; //帮助gc
                    tableDataParserCallback.call(tableData,hDatabasseRowModel);
                }else{
                    // 移动到 size
                    tableData.seek(tableData.getFilePointer() + 8 + 8);
                    int size =  tableData.readInt();
                    // 移动文件指针
                    tableData.seek(tableData.getFilePointer() + size);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                tableData.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 拿到 列 数据
     * @param columnBuff
     * @param iterator
     * @return
     */
    private HDatabaseColumnModel gethDatabaseColumnModel(ByteBuffer columnBuff, Iterator<String> iterator) {
        byte type =  columnBuff.get();
        int  size =  columnBuff.getInt();
        byte [] cdata = new byte[size];
        columnBuff.get(cdata);
        HDatabaseColumnModel columnModel = new HDatabaseColumnModel();
        columnModel.setType(type);
        columnModel.setSize(size);
        columnModel.setData(cdata);
        columnModel.setName(iterator.next());
        try {
            columnModel.setValue(byte2java(columnModel));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return columnModel;
    }


    /**
     * 列的二进制数据 转换成 java对象
     * @param databaseColumnModel
     * @return
     * @throws UnsupportedEncodingException
     */
    private Object byte2java(HDatabaseColumnModel databaseColumnModel) throws UnsupportedEncodingException {

        if(databaseColumnModel == null  || databaseColumnModel.getData().length == 0 ||  databaseColumnModel.getData() == null){
            return  null;
        }
        if (databaseColumnModel.getType() == ColumnVauleType.BIGINT.getValue()) {
            return ByteUtil.getLong(databaseColumnModel.getData());
        }else if (databaseColumnModel.getType() == ColumnVauleType.INT.getValue()) {
            return ByteUtil.getInt(databaseColumnModel.getData());
        }else if (databaseColumnModel.getType() == ColumnVauleType.FlOAT.getValue()) {
            return ByteUtil.getFloat(databaseColumnModel.getData());
        }else if (databaseColumnModel.getType() == ColumnVauleType.DOUBLE.getValue()) {
            return ByteUtil.getDouble(databaseColumnModel.getData());
        }else if (databaseColumnModel.getType() == ColumnVauleType.VARCHAR.getValue()) {
            return  new String(databaseColumnModel.getData(),"UTF-8");
        }else if (databaseColumnModel.getType() == ColumnVauleType.DATE.getValue()) {
            return  new String(databaseColumnModel.getData(),"UTF-8");
        }else if (databaseColumnModel.getType() == ColumnVauleType.DATETIME.getValue()) {
            return  new String(databaseColumnModel.getData(),"UTF-8");
        }else if (databaseColumnModel.getType() == ColumnVauleType.TIME.getValue()) {
            return  new String(databaseColumnModel.getData(),"UTF-8");
        }
        return  null;
    }


    /**
     * 列数据  转 byte
     * @param value
     * @param columnModel
     */
    protected void java2byte(Object value, HDatabaseColumnModel columnModel) {
        if (columnModel.getType() == ColumnVauleType.INT.getValue()) {
            columnModel.setData(ByteUtil.getBytes(Integer.parseInt(value.toString())));
        }else if (columnModel.getType() == ColumnVauleType.BIGINT.getValue()) {
            columnModel.setData(ByteUtil.getBytes(Long.parseLong(value.toString())));
        }else if (columnModel.getType() == ColumnVauleType.FlOAT.getValue()) {
            columnModel.setData(ByteUtil.getBytes(Float.parseFloat(value.toString())));
        }else if (columnModel.getType() == ColumnVauleType.DOUBLE.getValue()) {
            columnModel.setData(ByteUtil.getBytes(Double.parseDouble(value.toString())));
        }else if (columnModel.getType() == ColumnVauleType.DATE.getValue()) {
            Date date = DateUtils.pasre(value.toString(),DateUtils.DATE_PATTERN);
            if(null == date){
                throw  new RuntimeException("时间格式错误，正确格式 "+DateUtils.DATE_PATTERN);
            }
            columnModel.setData(ByteUtil.getBytes(DateUtils.format(date,DateUtils.DATE_PATTERN)));
        }else if (columnModel.getType() == ColumnVauleType.DATETIME.getValue()) {
            Date date = DateUtils.pasre(value.toString(),DateUtils.DATE_TIME_PATTERN);
            if(null == date){
                throw  new RuntimeException("时间格式错误，正确格式 "+DateUtils.DATE_TIME_PATTERN);
            }
            columnModel.setData(ByteUtil.getBytes(DateUtils.format(date,DateUtils.DATE_TIME_PATTERN)));
        }else if (columnModel.getType() == ColumnVauleType.TIME.getValue()) {
            Date date = DateUtils.pasre(value.toString(),DateUtils.TIME);
            if(null == date){
                throw  new RuntimeException("时间格式错误，正确格式 "+DateUtils.TIME);
            }
            columnModel.setData(ByteUtil.getBytes(DateUtils.format(date,DateUtils.TIME)));
        }else if (columnModel.getType() == ColumnVauleType.VARCHAR.getValue() || columnModel.getType() == ColumnVauleType.CHAR.getValue()) {
            columnModel.setData(ByteUtil.getBytes(value.toString()));
        }
    }


    /**
     * 打印一串横线
     * @param size
     * @return
     */
    protected String genLineString(int size) {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < (size * 17); i++) {
            tmp.append("-");
        }
        tmp.append("-");
        return tmp.toString();
    }




}
