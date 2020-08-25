package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BaseActivity";

    public static final String ROOT_PATH = "/sdcard/FactoryTest0718/";
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";
    public static final int SPRING_GREEN = Color.parseColor("#3CB371");

    public String _deviceType = "", _systemVersion = "";
    public Button _btnPass, _btnFail;
    public int _screenHeight, _screenWidth;
    //基类布局
    public LinearLayout _baseLinearLayout;
    public boolean _isInsertHeadset = false;
    public HeadsetBroadcasetReceiver _headsetBroadcasetReceiver;

    /**
     * 用于检测耳机是否插入
     */
    class HeadsetBroadcasetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //没有插入耳机
            if (intent.getIntExtra("state", 0) == 0) {
                _isInsertHeadset = false;
            }
            //已插入耳机
            else if (intent.getIntExtra("state", 0) == 1) {
                _isInsertHeadset = true;
            }
        }
    }

    public void TxtToTop(TextView txt) {
        txt.scrollTo(0, 0);
    }

    public void TxtToBottom(TextView txt) {
        int offset = txt.getLineCount() * txt.getLineHeight();
        if (offset > txt.getHeight()) {
            txt.scrollTo(0, offset - txt.getHeight());
        }
    }

    public void TxtClear(TextView txt) {
        txt.setText("");
        txt.scrollTo(0, 0);
    }

    /**
     * 测试通过
     */
    public void Pass() {
        Intent intent = new Intent();
        intent.putExtra(ARG_PARAM1, PASS);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 测试失败
     */
    public void Fail() {
        Intent intent = new Intent();
        intent.putExtra(ARG_PARAM1, FAIL);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 全部被允许
     */
    public boolean CheckPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    /**
     * 将继承自BaseActivity的layout加进来
     *
     * @param layoutId
     */
    public void SetBaseContentView(int layoutId) {
        _baseLinearLayout = findViewById(R.id.ll_base);
        //根据屏幕尺寸设置控件大小，不然基类的控件可能看不到
        ViewGroup.LayoutParams layoutParams = _baseLinearLayout.getLayoutParams();
        layoutParams.height = _screenHeight - 400;
        //将继承自该Activity的布局加进来
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(layoutId, null);
        _baseLinearLayout.addView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_headsetBroadcasetReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, FAIL);
            setResult(RESULT_OK, intent);
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pass_base:
                Pass();
                break;
            case R.id.btn_fail_base:
                Fail();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去年系统的TitleBar
        //        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_base);
        //Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        _btnPass = findViewById(R.id.btn_pass_base);
        _btnPass.setOnClickListener(this::onClick);
        _btnFail = findViewById(R.id.btn_fail_base);
        _btnFail.setOnClickListener(this::onClick);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);
        _screenHeight = point.y;
        _screenWidth = point.x;
        //检测耳机是否插入
        _headsetBroadcasetReceiver = new HeadsetBroadcasetReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(_headsetBroadcasetReceiver, intentFilter);
    }
}
