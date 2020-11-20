package com.haojiangbo.ndkdemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.haojiangbo.utils.android.StatusBarColorUtils;

public class RigisterNumber extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rigister_number);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar){
            actionBar.hide();
        }
        StatusBarColorUtils.setBarColor(this,R.color.design_default_color_background);


    }

    @Override
    public void onClick(View v) {

    }
}