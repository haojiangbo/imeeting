package com.haojiangbo.option;

/**
 * 左值比较器
 * <p>
 * 比如 a = 1
 * 这个符号 a 代表的值
 * 需要这个接口的实现类
 * 取解析
 * 并且需要实现对应的比较
 * 方法
 * <p>
 * 　　* @author 郝江波
 * 　　* @date 2020/10/26 13:56
 *
 */
public interface LeftValueParserInteface<T,O> {

    O parserData(String leftValueKeyWord, T columnModel);

    boolean equality(Object value);

    boolean Like(Object value);

    boolean SoudsLike(Object value);

    boolean NotEqual(Object value);

    boolean GreaterThan(Object value);

    boolean GreaterThanOrEqual(Object value);

    boolean LessThan(Object value);

    boolean LessThanOrEqual(Object value);

    boolean LessThanOrGreater(Object value);

    boolean NotLike(Object value);

    boolean NotLessThan(Object value);

    boolean NotGreaterThan(Object value);

    boolean RLike(Object value);

    boolean NotRLike(Object value);

    boolean RegExp(Object value);

    boolean NotRegExp(Object value);

    boolean Is(Object value);

    boolean IsNot(Object value);

}
