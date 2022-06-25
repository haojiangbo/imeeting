package com.haojiangbo.ndkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haojiangbo.application.MyApplication;
import com.haojiangbo.audio.AudioRecorder;
import com.haojiangbo.camera.CameraActivity;
import com.haojiangbo.eventbus.MessageModel;
import com.haojiangbo.ffmpeg.VideoEncode;
import com.haojiangbo.net.MediaProtocolManager;
import com.haojiangbo.net.protocol.ControlProtocol;
import com.haojiangbo.net.protocol.MessageAck;
import com.haojiangbo.net.protocol.Pod;
import com.haojiangbo.net.protocol.RoomInfo;
import com.haojiangbo.net.tcp.ControlProtocolManager;
import com.haojiangbo.net.tcp.hander.IdleCheckHandler;
import com.haojiangbo.screen.ScreenRecordActivity;
import com.haojiangbo.service.AudioPalyService;
import com.haojiangbo.storage.NumberStorageManager;
import com.haojiangbo.thread.MeidaParserInstand;
import com.haojiangbo.thread.ParserMediaProtoThreadPool;
import com.haojiangbo.thread.VideoMediaParserInstand;
import com.haojiangbo.utils.ToastUtils;
import com.haojiangbo.utils.aes.AesEncodeUtil;
import com.haojiangbo.utils.android.StatusBarColorUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    // 自己的号码
    public static String ROOM_NUMBER = null;
    // 对方的号码
    public static String TARGET_NUMBER = null;
    ControlProtocolManager controlProtocolManager = new ControlProtocolManager();
    MediaProtocolManager mediaProtocolManager = new MediaProtocolManager();

    // 案件缓存
    private static List<String> KEY_WORD_CATCH = new ArrayList<>(6);

    // Used to load the 'native-lib' library on application startup.
    static {
        // avutil avresample swresample swscale avcodec avformat avfilter avdevice
        System.loadLibrary("mp3lame");
        System.loadLibrary("native-lib");
    }

    private Button create;
    private Button join;
    private EditText roomNum;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }
        //StatusBarColorUtils.setBarColor(this, R.color.heise,false);
        StatusBarColorUtils.setBarColor(this, R.color.design_default_color_background);
        EventBus.getDefault().register(this);
        create = findViewById(R.id.create);
        join = findViewById(R.id.join);
        roomNum = findViewById(R.id.roomnum);
        password = findViewById(R.id.password);
        // 初始化权限
        if (!initPermission()) {
            return;
        }
        create.setOnClickListener(view ->
                {
                    String rr = roomNum.getText().toString();
                    String ps = password.getText().toString();
                    if (StringUtil.isNullOrEmpty(rr) || StringUtil.isNullOrEmpty(ps)) {
                        ToastUtils.showToastShort("请输入房间号和密码");
                        return;
                    }
                    MainActivity.ROOM_NUMBER = rr;
                    RoomInfo roomInfo = new RoomInfo();
                    roomInfo.setRoomnum(rr);
                    roomInfo.setPassword(ps);
                    if(ControlProtocolManager.ACTIVITY_CHANNEL == null || !ControlProtocolManager.ACTIVITY_CHANNEL.isActive()){
                        ToastUtils.showToastShort("服务不可用");
                        return;
                    }
                    ControlProtocolManager.ACTIVITY_CHANNEL
                            .writeAndFlush(new ControlProtocol(ControlProtocol.CREATE, roomInfo.toJson().getBytes()));
                }
        );
        join.setOnClickListener(view -> {
            String rr = roomNum.getText().toString();
            String ps = password.getText().toString();
            if (StringUtil.isNullOrEmpty(rr) || StringUtil.isNullOrEmpty(ps)) {
                ToastUtils.showToastShort("请输入房间号和密码");
                return;
            }
            MainActivity.ROOM_NUMBER = rr;
            RoomInfo roomInfo = new RoomInfo();
            roomInfo.setRoomnum(rr);
            roomInfo.setPassword(ps);
            ControlProtocolManager.ACTIVITY_CHANNEL
                    .writeAndFlush(new ControlProtocol(ControlProtocol.JOIN, roomInfo.toJson().getBytes()));
        });
        // 初始化音频录制
        initAudioRecorder();
        // 初始化网络配置
        initNetworkConfig();
    }

    @Override
    protected void onStart() {
        // 获取配置信息
        //initConfigInfo();
        super.onStart();
    }

    private void initNetworkConfig() {
        // tcp初始化
        controlProtocolManager.start();

        // udp初始化
        mediaProtocolManager.start();
    }


    private void initAudioRecorder() {
        AudioRecorder.getInstance().createDefaultAudio();
    }



    /**
     * 收到呼叫消息之后，需要做一个接通的操作
     *
     * @param message
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mssageEventBus(final MessageModel message) {
        String payLoad = (String) message.payLoad;
        if (StringUtil.isNullOrEmpty(payLoad)) {
            return;
        }
        MessageAck messageAck = JSONObject.parseObject(payLoad, MessageAck.class);
        ToastUtils.showToastShort(messageAck.getMsg());
        MettingActivite mettingActivite = MettingActivite.getInstand();
        if (message.type == ControlProtocol.CREATE_REPLAY || message.type == ControlProtocol.JOIN_REPLAY) {
            if (messageAck.getCode() != 0) {
                return;
            }
            JSONArray data = (JSONArray) messageAck.getData();
            MainActivity.ROOM_NUMBER = roomNum.getText().toString();
            IdleCheckHandler.sendPingMessage(message.channel);
            Intent intentv = new Intent(this, MettingActivite.class);
            ArrayList arrayList = new ArrayList(data.size());
            if (null != data && data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    arrayList.add(data.getString(i));
                    Log.e("uid = ", data.getString(i));
                }
                intentv.putStringArrayListExtra("meetingUids", arrayList);
            }
            if (MettingActivite.getInstand() == null) {
                startActivity(intentv);
            } else {
                if (null != mettingActivite) {
                    mettingActivite.addVideoSurface(arrayList,false);
                }
            }
        } else if (message.type == ControlProtocol.CLOSE) {
            if (null != mettingActivite) {
                mettingActivite.removeVideoSurface(messageAck.getData().toString());
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_recorder:
                AudioRecorder.getInstance().startRecord();
                break;
            case R.id.end_recorder:
                AudioRecorder.getInstance().stopRecord();
                break;
            case R.id.register_number:
                Intent intent = new Intent(this, RigisterNumber.class);
                startActivity(intent);
                break;
            case R.id.open_video_test:
                Intent intentv = new Intent(this, CameraActivity.class);
                startActivity(intentv);
                break;
            case R.id.open_scrren_test:
                Intent intentb = new Intent(this, ScreenRecordActivity.class);
                startActivity(intentb);
                break;
            default:
                viewHander(v);
        }
    }

    private void viewHander(View view) {
        if (view instanceof Button) {
            Button button = (Button) view;
            String keyWord = button.getText().toString();

            switch (keyWord) {
                case "CALL":
                    String result = list2str(KEY_WORD_CATCH);
                    if (result.getBytes().length != 6) {
                        Toast.makeText(MyApplication.getContext(), "仅支持6位电话号码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent call = new Intent(this, Call.class);
                    call.putExtra("src", MainActivity.ROOM_NUMBER);
                    call.putExtra("dst", result);
                    call.putExtra("type", Call.attack);
                    startActivity(call);
                    break;
                case "DEL":
                    if (KEY_WORD_CATCH.size() > 0) {
                        KEY_WORD_CATCH.remove(KEY_WORD_CATCH.size() - 1);
                    }
                    break;
                default:
                    KEY_WORD_CATCH.add(keyWord);
            }
            if (KEY_WORD_CATCH.size() > 6) {
                KEY_WORD_CATCH.remove(KEY_WORD_CATCH.size() - 1);
                Toast.makeText(MyApplication.getContext(), "最大支持6位电话号码", Toast.LENGTH_SHORT).show();
            }
            String result = list2str(KEY_WORD_CATCH);

        }
    }

    private <T> String list2str(List<T> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (T item : list) {
            stringBuilder.append(item);
        }
        return stringBuilder.toString();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaProtocolManager.stop();
        controlProtocolManager.stop();
        EventBus.getDefault().unregister(this);
    }


    //////////////////////////////////////动态申请权限模块///////////////////////////////////////

    String[] quanxianList = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    /* Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
     Manifest.permission.ACCESS_FINE_LOCATION,
     Manifest.permission. READ_PHONE_STATE,*/
    private final int mRequestCode = 100;//权限请求码

    private boolean initPermission() {
        int sta = 0;
        for (String s : quanxianList) {
            if (ContextCompat.checkSelfPermission(this, s) != PERMISSION_GRANTED) {
                sta++;
            }
        }
        if (sta > 0) {
            ActivityCompat.requestPermissions(this, quanxianList, mRequestCode);
        }
        return sta > 0 ? false : true;
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog();/// 跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
                //this.finish();
            }
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;
    private static final int NOT_NOTICE = 2;//如果勾选了不再询问

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();
                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivityForResult(intent, NOT_NOTICE);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            MainActivity.this.finish();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    //关闭对话框
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

}