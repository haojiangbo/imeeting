package com.haojiangbo.ndkdemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.haojiangbo.application.MyApplication;
import com.haojiangbo.eventbus.MessageModel;
import com.haojiangbo.storage.NumberStorageManager;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.utils.aes.AesEncodeUtil;
import com.haojiangbo.utils.android.StatusBarColorUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

/**
 * 注册电话号码
 */
public class RigisterNumber extends AppCompatActivity implements View.OnClickListener {

    private EditText rigisterInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rigister_number);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }
        StatusBarColorUtils.setBarColor(this, R.color.design_default_color_background);
        rigisterInput = findViewById(R.id.rigister_input);
        byte[] bytes = NumberStorageManager.build().getData("number.cnf");
        if (null != bytes) {
            String data = AesEncodeUtil.decode(new String(bytes));
            if (data.length() >= 6){
                rigisterInput.setText(data);
                rigisterInput.setEnabled(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(rigisterInput.getText())
                || rigisterInput.getText().toString().length() != 6
        ) {
            Toast.makeText(MyApplication.getContext(), "请输入6位电话号码", Toast.LENGTH_SHORT).show();
            return;
        }
        if(rigisterInput.isEnabled()){
            String number = AesEncodeUtil.enCode(rigisterInput.getText().toString());
            NumberStorageManager.build().putData("number.cnf", number.getBytes());
        }else{
            ToastUtils.showToastShort("您已经注册过电话号码");
        }
        this.finish();
    }
}