package com.haojiangbo.ndkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

/**
 * 安卓 ffmpeg 编译
 *  https://www.jianshu.com/p/0a7f3175c1b9
 *  https://www.jianshu.com/p/276abc1ffbc6
 *  参考代码
 *  https://www.jianshu.com/p/f41cfc337f93
 *
 *  JNI环境参考
 *
 *
 *  编译环境 成功跑通的
 *  https://juejin.im/post/6844903945496690696
 *
 *
 *  代码地址
 * https://github.com/coopsrc/FFPlayerDemo/tree/master/app/src/main/java/cc/dewdrop/ffplayer
 *
 */
public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    public native String message();
}