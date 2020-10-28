package com.haojiangbo.option.imp;

import com.haojiangbo.common.ColumnVauleType;
import com.haojiangbo.datamodel.HDatabaseColumnModel;
import com.haojiangbo.datamodel.HDatabasseRowModel;
import com.haojiangbo.option.LeftValueParserInteface;

import java.io.UnsupportedEncodingException;

/**
 * HDatabasse 左值解析器
 * 　　* @author 郝江波
 * 　　* @date 2020/10/26 14:06
 *
 */
public class HDatabaseDruidASTLeftValueParser
        implements LeftValueParserInteface<HDatabasseRowModel, HDatabaseDruidASTLeftValueParser> {

    private String leftValueKeyWord;
    HDatabasseRowModel databasseRowModel;


    @Override
    public HDatabaseDruidASTLeftValueParser parserData(String leftValueKeyWord, HDatabasseRowModel columnModel) {
        this.databasseRowModel = columnModel;
        this.leftValueKeyWord = leftValueKeyWord;
        return this;
    }

    @Override
    public boolean equality(Object value) {
        try {
            Object[] tmp =  converData(value);
            if(null == tmp){
                return false;
            }
            if(tmp[0] instanceof  Comparable){
               return  ((Comparable) tmp[0]).compareTo(tmp[1]) == 0;
            }else if(tmp[0] instanceof  String){
                return tmp[0].equals(tmp[1]);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean Like(Object value) {
        throw new RuntimeException("暂不支持 like 操作");
    }

    @Override
    public boolean SoudsLike(Object value) {
        throw new RuntimeException("暂不支持 SoudsLike 操作");
    }

    @Override
    public boolean NotEqual(Object value) {
        return  !equality(value);
    }

    @Override
    public boolean GreaterThan(Object value) {
        try {
            Object[] tmp =  converData(value);
            if(null == tmp){
                return false;
            }
            if(tmp[0] instanceof  Comparable){
                return   ((Comparable) tmp[0]).compareTo(tmp[1]) > 0;
            }else {
               throw  new RuntimeException("不支持比较的类型"+tmp.getClass().getName());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("暂不支持 GreaterThan 操作");
    }

    @Override
    public boolean GreaterThanOrEqual(Object value) {
        try {
            Object[] tmp =  converData(value);
            if(null == tmp){
                return false;
            }
            if(tmp[0] instanceof  Comparable){
                return   ((Comparable) tmp[0]).compareTo(tmp[1]) > 0 || ((Comparable) tmp[0]).compareTo(tmp[1]) == 0;
            }else {
                throw  new RuntimeException("不支持比较的类型"+tmp.getClass().getName());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("暂不支持 GreaterThanOrEqual 操作");
    }

    @Override
    public boolean LessThan(Object value) {
        try {
            Object[] tmp =  converData(value);
            if(null == tmp){
                return false;
            }
            if(tmp[0] instanceof  Comparable){
                return   ((Comparable) tmp[0]).compareTo(tmp[1]) < 0;
            }else {
                throw  new RuntimeException("不支持比较的类型"+tmp.getClass().getName());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("暂不支持 LessThan 操作");
    }

    @Override
    public boolean LessThanOrEqual(Object value) {
        try {
            Object[] tmp =  converData(value);
            if(null == tmp){
                return false;
            }
            if(tmp[0] instanceof  Comparable){
                return   ((Comparable) tmp[0]).compareTo(tmp[1]) < 0 || ((Comparable) tmp[0]).compareTo(tmp[1]) == 0;
            }else {
                throw  new RuntimeException("不支持比较的类型"+tmp.getClass().getName());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("暂不支持 LessThanOrEqual 操作");
    }

    @Override
    public boolean LessThanOrGreater(Object value) {
        throw new RuntimeException("暂不支持 LessThanOrGreater 操作");
    }

    @Override
    public boolean NotLike(Object value) {
        throw new RuntimeException("暂不支持 NotLike 操作");
    }

    @Override
    public boolean NotLessThan(Object value) {
        return !LessThan(value);
    }

    @Override
    public boolean NotGreaterThan(Object value) {
        return !GreaterThan(value);
    }

    @Override
    public boolean RLike(Object value) {
        throw new RuntimeException("暂不支持 RLike 操作");
    }

    @Override
    public boolean NotRLike(Object value) {
        throw new RuntimeException("暂不支持 NotRLike 操作");
    }

    @Override
    public boolean RegExp(Object value) {
        throw new RuntimeException("暂不支持 RegExp 操作");
    }

    @Override
    public boolean NotRegExp(Object value) {
        throw new RuntimeException("暂不支持 NotRegExp 操作");
    }

    @Override
    public boolean Is(Object value) {
        throw new RuntimeException("暂不支持 Is 操作");
    }

    @Override
    public boolean IsNot(Object value) {
        throw new RuntimeException("暂不支持 IsNot 操作");
    }

    /**
     * 转换数据
     * @return
     * @throws UnsupportedEncodingException
     */
    private Object[] converData(Object rigth) throws UnsupportedEncodingException {
        HDatabaseColumnModel databaseColumnModel = this.databasseRowModel.getData().get(this.leftValueKeyWord);
        Object[] result = new Object[2];
        if(databaseColumnModel == null  || databaseColumnModel.getData() == null){
            return  null;
        }
        if (databaseColumnModel.getType() == ColumnVauleType.BIGINT.getValue()) {
            result[0] = databaseColumnModel.getValue();
            result[1] = Long.parseLong(rigth.toString());
            databaseColumnModel.setValue(result[0]);
            return result;
        }else if (databaseColumnModel.getType() == ColumnVauleType.INT.getValue()) {
            result[0] = databaseColumnModel.getValue();
            result[1] = Integer.parseInt(rigth.toString());
            databaseColumnModel.setValue(result[0]);
            return result;
        }else if (databaseColumnModel.getType() == ColumnVauleType.VARCHAR.getValue() || databaseColumnModel.getType() == ColumnVauleType.CHAR.getValue()) {
            result[0] = databaseColumnModel.getValue();
            result[1] = rigth.toString();
            databaseColumnModel.setValue(result[0]);
            return result;
        }else if (databaseColumnModel.getType() == ColumnVauleType.DATE.getValue()) {
             throw  new RuntimeException("不支持的类型");
        }
        return  null;
    }

}
