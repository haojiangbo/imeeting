package com.haojiangbo.parser.imp;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExplainStatement;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.haojiangbo.common.ColumnVauleType;
import com.haojiangbo.datamodel.HDatabaseTableModel;
import com.haojiangbo.option.LeftValueParserInteface;
import com.haojiangbo.parser.CommonStatementParser;
import com.haojiangbo.parser.StatementParserInteface;

import java.io.RandomAccessFile;
import java.util.Map;

/**
 * Explain 解释器
 *
 * @author 郝江波
 * @date 2020/10/28 17:16
 */
public class ExplainStatementParser extends CommonStatementParser
        implements StatementParserInteface<MySqlExplainStatement, Boolean> {
    @Override
    protected LeftValueParserInteface getLeftValueParser() {
        return null;
    }

    @Override
    public Boolean parser(MySqlExplainStatement statement) {
        String tableName = statement.getTableName().getSimpleName().toUpperCase();
        if (statement.isDescribe()) {
            RandomAccessFile metaDataFile = getTableMetaDataFile(tableName, "r");
            if (null == metaDataFile) {
                System.out.println(tableName + "不存在");
                return false;
            }
            byte[] metaData = getTableMetaDataArray(metaDataFile);
            JSONObject metaJson = JSONObject.parseObject(new String(metaData), Feature.OrderedField);
            int titleLength = 2;

            System.out.println(genLineString(titleLength));
            System.out.print("|");
            System.out.print(String.format(" %-15s|", "字段"));
            System.out.println(String.format(" %-15s|", "类型"));
            System.out.println(genLineString(titleLength));
            for (Map.Entry<String, Object> entry : metaJson.entrySet()) {
                StringBuilder tmp = new StringBuilder();
                tmp.append("|");
                tmp.append(String.format(" %-15s|", entry.getKey()));
                if (entry.getKey().equals(HDatabaseTableModel.PRIMARY)) {
                    tmp.append(String.format(" %-15s|", entry.getValue().toString()));
                } else {
                    tmp.append(String.format(" %-15s|", ColumnVauleType.caseByteStringValue(metaJson.getByte(entry.getKey()).byteValue())));
                }
                System.out.println(tmp.toString());
            }

            System.out.println(genLineString(titleLength));
            return true;
        }
        System.out.println("该命令暂不支持...");
        return false;
    }
}
