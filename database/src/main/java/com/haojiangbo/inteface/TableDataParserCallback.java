package com.haojiangbo.inteface;

public interface TableDataParserCallback<D,T> {
    void call(D data, T t);
}
