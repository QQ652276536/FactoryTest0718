package com.zistone.factorytest0718;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyProgressDialogUtil;
import com.zistone.factorytest0718.util.MyScanCodeManager;
import com.zistone.factorytest0718.util.MySoundPlayUtil;

import java.io.File;
import java.util.List;

/**
 * 扫码测试，只支持誉兴通的设备
 *
 * @author LiWei
 * @date 2020/9/3 18:50
 * @email 652276536@qq.com
 */
public class ScanCodeActivity_Temp extends BaseActivity {

    private static final String TAG = "ScanCodeActivity_Temp";

    private TextView _txt;
    private ImageButton _btnTop, _btnBottom, _btnClear;

    private void UpdateText(final TextView txt, final String str, final String setOrAppend) {
        if (null == txt)
            return;
        runOnUiThread(() -> {
            switch (setOrAppend) {
                case "Set":
                    txt.setText(str);
                    break;
                case "Append":
                    txt.append(str);
                    TxtToBottom(txt);
                    break;
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
        MyScanCodeManager.StartReadThread_Temp();
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
        MyScanCodeManager.StopReadThread_Temp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_scancode_temp);
        SetBaseContentView(R.layout.activity_scancode_temp);
        _btnPass.setEnabled(false);
        _txt = findViewById(R.id.txt_scancode);
        _txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btnTop = findViewById(R.id.btn_top_scancode);
        _btnTop.setOnClickListener(v -> TxtToTop(_txt));
        _btnBottom = findViewById(R.id.btn_bottom_scancode);
        _btnBottom.setOnClickListener(v -> TxtToBottom(_txt));
        _btnClear = findViewById(R.id.btn_clear_scancode);
        _btnClear.setOnClickListener(v -> TxtClear(_txt));
        MyScanCodeManager.SetListener((data, len) -> {
            if (null != data && data.length > 0 && len > 0) {
                String obj = ConvertCharToString(data, len);
                Log.i(TAG, "扫描到的数据：" + obj);
                UpdateText(_txt, obj + "\n", "Append");
                runOnUiThread(() -> {
                    _btnPass.setEnabled(true);
                    MyScanCodeManager.StopReadThread_Temp();
                    MySoundPlayUtil.SystemSoundPlay(ScanCodeActivity_Temp.this);
                    MyProgressDialogUtil.ShowCountDownTimerWarning(ScanCodeActivity_Temp.this, "知道了", 3 * 1000, "提示", "扫码测试已通过！\n\n扫描数据：" + obj, false, () -> {
                        MyProgressDialogUtil.DismissAlertDialog();
                        Pass();
                    });
                });
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
    }

}
