package com.haojiangbo.utils;

import android.view.Gravity;
import android.widget.Toast;

import com.haojiangbo.application.MyApplication;

/**
 * Created by Administrator on 2020/5/22.
 */

public class ToastUtils {
    public static void showToastShort(String message){
        Toast toast =  Toast.makeText(MyApplication.getContext(),message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showToastLong(String message){
        Toast.makeText(MyApplication.getContext(),message, Toast.LENGTH_LONG).show();
    }

}
