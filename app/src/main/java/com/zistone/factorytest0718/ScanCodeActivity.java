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

public class ScanCodeActivity extends BaseActivity implements View.OnClickListener, MyScanCodeManager.ScanCodeListener {

    private static final String TAG = "ScanCodeActivity";

    private TextView _txt;
    private ImageButton _btnTop, _btnBottom, _btnClear;
    private Button _btnScan;
    private boolean _isAppOnForeground = true;

    @SuppressLint("HandlerLeak")
    private Handler _handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i(TAG, "msg.what = " + msg.what);
            Log.i(TAG, "msg.obj = " + msg.obj);
            switch (msg.what) {
                case 1:
                    UpdateText(_txt, msg.obj + "\r\n", "Append");
                    MySoundPlayUtil.SystemSoundPlay(ScanCodeActivity.this);
                    break;
            }
        }
    };

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
                        int offset = txt.getLineCount() * txt.getLineHeight();
                        if (offset > txt.getHeight())
                            txt.scrollTo(0, offset - txt.getHeight());
                        break;
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
    public void onReceived(byte[] data, int len) {
        Log.i(TAG, "len = " + len);
        Log.i(TAG, "data = " + data);
        if (len <= 0 || data == null)
            return;
        if (data != null && data.length > 1 && len > 0) {
            String obj = ConvertCharToString(data, len);
            Log.i(TAG, "msg.obj = " + obj);
            _handler.obtainMessage(1, obj).sendToTarget();
        }
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
        _handler = null;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_top_scancode:
                _txt.scrollTo(0, 0);
                break;
            case R.id.btn_bottom_scancode:
                int offset = _txt.getLineCount() * _txt.getLineHeight();
                if (offset > _txt.getHeight()) {
                    _txt.scrollTo(0, offset - _txt.getHeight());
                }
                break;
            case R.id.btn_clear_scancode:
                _txt.setText("");
                break;
            case R.id.btn_scan_scancode:
                _btnScan.setEnabled(false);
                MyScanCodeManager.StopReadThread();
                MyScanCodeManager.StartReadThread();
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                _btnScan.setEnabled(true);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_scancode);
        SetBaseContentView(R.layout.activity_scancode);
        _txt = findViewById(R.id.txt_scancode);
        _txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnTop = findViewById(R.id.btn_top_scancode);
        _btnTop.setOnClickListener(this::onClick);
        _btnBottom = findViewById(R.id.btn_bottom_scancode);
        _btnBottom.setOnClickListener(this::onClick);
        _btnClear = findViewById(R.id.btn_clear_scancode);
        _btnClear.setOnClickListener(this::onClick);
        _btnScan = findViewById(R.id.btn_scan_scancode);
        _btnScan.setOnClickListener(this::onClick);
        MyScanCodeManager.Init(this::onReceived);
        try {
            MyScanCodeManager.OpenSerialPort(new File("/dev/ttyHSL1"), 9600, 0);
            UpdateText(_txt, "串口已打开\r\n", "Append");
        } catch (Exception e) {
            MyProgressDialogUtil.ShowWarning(this, "警告", "该设备不支持扫码，无法使用此功能！", false, () -> {
                Intent intent = new Intent();
                intent.putExtra(ARG_PARAM1, FAIL);
                setResult(RESULT_OK, intent);
                finish();
            });
        }
        _btnPass.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, PASS);
            setResult(RESULT_OK, intent);
            finish();
        });
        _btnFail.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, FAIL);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

}
