package com.haojiangbo.application;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class MyApplication  extends Application {
    private static Context context;
    private static SQLiteDatabase db;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /**
     * 获取全局上下文
     * @return
     */
    public static Context getContext() {
        return context;
    }

    public static SQLiteDatabase getDb() {
        return db;
    }
}
