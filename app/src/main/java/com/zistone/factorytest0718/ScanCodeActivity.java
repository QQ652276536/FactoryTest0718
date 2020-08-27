package com.zistone.factorytest0718;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.factorytest0718.util.MyProgressDialogUtil;
import com.zistone.factorytest0718.util.MyScanCodeManager;
import com.zistone.factorytest0718.util.MySoundPlayUtil;

import java.io.File;
import java.util.List;

/**
 * 扫码测试，只支持誉兴通的设备
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class ScanCodeActivity extends BaseActivity {

    private static final String TAG = "ScanCodeActivity";

    private TextView _txt;
    private ImageButton _btnTop, _btnBottom, _btnClear;
    private Button _btnScan;
    private boolean _isAppOnForeground = true, _isPass = false;

    private void UpdateText(final TextView txt, final String str, final String setOrAppend) {
        if (null == txt)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (setOrAppend) {
                    case "Set":
                        txt.setText(str);
                        break;
                    case "Append":
                        txt.append(str);
                        TxtToBottom(txt);
                        break;
                }
                if (_isPass) {
                    //防止重复触发
                    _isPass = false;
                    _btnPass.setEnabled(true);
                    _btnScan.setEnabled(true);
                    MyScanCodeManager.StopReadThread();
                    MySoundPlayUtil.SystemSoundPlay(ScanCodeActivity.this);
                    MyProgressDialogUtil.ShowCountDownTimerWarning(ScanCodeActivity.this, "知道了", 3 * 1000, "提示", "扫码测试已通过！\n\n扫描数据：" + str, false, () -> {
                        MyProgressDialogUtil.DismissAlertDialog();
                        Pass();
                    });
                }
            }
        });
    }

    public String ConvertCharToString(byte[] buf, int len) {
        String barcodeMsg = "";
        for (int i = 0; i < len; i++) {
            if (buf[i] != 0) {
                if (buf[i] != '\n' || buf[i] != '\r')
                    //ASCII码转换底层返回的字节数组数据
                    barcodeMsg += (char) (buf[i]);
            }
        }
        return barcodeMsg;
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    private boolean IsAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //进入后台后再次回到前台
        if (!_isAppOnForeground) {
            MyScanCodeManager.StopReadThread();
            MyScanCodeManager.StartReadThread();
            _isAppOnForeground = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyScanCodeManager.Close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        _isAppOnForeground = IsAppOnForeground();
        if (!_isAppOnForeground) {
            MyScanCodeManager.StopReadThread();
            UpdateText(_txt, "\r\n程序进入后台，扫描已停止！", "Append");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_scancode);
        SetBaseContentView(R.layout.activity_scancode);
        _txt = findViewById(R.id.txt_scancode);
        _txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnTop = findViewById(R.id.btn_top_scancode);
        _btnTop.setOnClickListener(v -> TxtToTop(_txt));
        _btnBottom = findViewById(R.id.btn_bottom_scancode);
        _btnBottom.setOnClickListener(v -> TxtToBottom(_txt));
        _btnClear = findViewById(R.id.btn_clear_scancode);
        _btnClear.setOnClickListener(v -> TxtClear(_txt));
        _btnScan = findViewById(R.id.btn_scan_scancode);
        _btnScan.setOnClickListener(v -> {
            _btnScan.setEnabled(false);
            MyScanCodeManager.StartReadThread();
        });
        MyScanCodeManager.Init((data, len) -> {
            if (null == data || len == 0) {
                _btnScan.setEnabled(true);
                return;
            }
            if (null != data && data.length > 0 && len > 0) {
                String obj = ConvertCharToString(data, len);
                Log.i(TAG, "扫描到的数据：" + obj);
                _isPass = true;
                UpdateText(_txt, obj + "\n", "Append");
            }
        });
        try {
            MyScanCodeManager.OpenSerialPort(new File("/dev/ttyHSL1"), 9600, 0);
            UpdateText(_txt, "串口已打开\r\n", "Append");
        } catch (Exception e) {
            MyProgressDialogUtil.ShowWarning(this, "知道了", "警告", "该设备不支持扫码，无法使用此功能！", false, () -> {
                Fail();
            });
        }
        _btnPass.setEnabled(false);
        //触发点击事件
        _btnScan.performClick();
    }

}
