package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyConvertUtil;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;
import com.zistone.factorytest0718.util.MySerialPortManager;
import com.zistone.factorytest0718.util.MySharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ComTestActivity extends BaseActivity {

    private static final String TAG = "ComTestActivity";
    private static final int TASKTIME = 1 * 1000;

    private TextView _txtMessag;
    private ImageButton _imgBtnTop, _imgBtnBottom, _imgBtnClear;
    private Button _btnSend;
    private EditText _edtPortName, _edtData;
    private Spinner _spinnerRate;
    private Timer _timer;
    private TimerTask _timerTask;
    private String _portName, _hexData;
    private int _baudRate;
    private boolean _isAppOnForeground = true;

    /**
     * 定时任务的内容
     */
    private void InitTimerTask() {
        try {
            Log.i(TAG, "定时任务执行...");
            byte[] byteArray = MyConvertUtil.HexStrToByteArray(_hexData);
            MySerialPortManager.SendData(byteArray);
            Thread.sleep(100);
            //没有数据时这里会阻塞
            byte[] bytes = MySerialPortManager.ReadData();
            if (bytes != null && bytes.length > 0) {
                String dataStr = MyConvertUtil.ByteArrayToHexStr(bytes);
                Log.i(TAG, "收到串口数据：" + dataStr);
                UpdateText(_txtMessag, "收到串口数据：" + dataStr + "\r\n", "Append");
            } else {
                Log.i(TAG, "没有收到串口数据，请检查设备是否支持该串口或串口被占用！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            _timer.cancel();
            _timerTask.cancel();
            UpdateText(_txtMessag, "串口通讯异常：" + e.toString() + "\r\n", "Append");
            UpdateBtn(_btnSend, "开始发送");
        }
    }

    private void UpdateBtn(final Button btn, final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != str && !"".equals(str))
                    btn.setText(str);
            }
        });
    }

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
            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        if (null != _timer)
            _timer.cancel();
        if (null != _timerTask)
            _timerTask.cancel();
        MySerialPortManager.Close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!_isAppOnForeground)
            _isAppOnForeground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //记录当前已经进入后台
        _isAppOnForeground = IsAppOnForeground();
        if (!_isAppOnForeground) {
            if (null != _timer)
                _timer.cancel();
            if (null != _timerTask)
                _timerTask.cancel();
            MySerialPortManager.Close();
            UpdateBtn(_btnSend, "开始发送");
            UpdateText(_txtMessag, "\r\n程序进入后台，停止发送数据且串口关闭！", "Append");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_comtest);
        SetBaseContentView(R.layout.activity_comtest);
        String str = MySharedPreferences.GetSerialPortNameAndBaudrate(this);
        String[] strArray = str.split(",");
        _portName = strArray[0];
        _baudRate = Integer.parseInt(strArray[1]);
        _hexData = strArray[2];
        _edtPortName = findViewById(R.id.edt_name_com);
        _edtPortName.setText(_portName);
        _edtPortName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                _portName = s.toString();
                MySharedPreferences.SetSerialPortNameAndBaudrate(ComTestActivity.this, _portName + "," + _baudRate + "," + _hexData);
            }
        });
        _edtData = findViewById(R.id.edt_data_com);
        _edtData.setText(_hexData);
        _edtData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                _hexData = s.toString();
                MySharedPreferences.SetSerialPortNameAndBaudrate(ComTestActivity.this, _portName + "," + _baudRate + "," + _hexData);
            }
        });
        _spinnerRate = findViewById(R.id.spinner_rate_com);
        //外层大括号相当于new接口
        List<String> itemList = new ArrayList<String>() {
            //内层大括号相当于构造代码块
            {
                add("115200");
                add("57600");
                add("56000");
                add("43000");
                add("38400");
                add("19200");
            }
        };
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, itemList);
        _spinnerRate.setAdapter(arrayAdapter);
        _spinnerRate.setSelection(arrayAdapter.getPosition(_baudRate + ""), true);
        _spinnerRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _baudRate = Integer.parseInt(_spinnerRate.getSelectedItem().toString());
                MySharedPreferences.SetSerialPortNameAndBaudrate(ComTestActivity.this, _portName + "," + _baudRate + "," + _hexData);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        _txtMessag = findViewById(R.id.txt_message_com);
        _txtMessag.setMovementMethod(ScrollingMovementMethod.getInstance());
        _imgBtnTop = findViewById(R.id.btn_top_com);
        _imgBtnTop.setOnClickListener(v -> {
            TxtToTop(_txtMessag);
        });
        _imgBtnBottom = findViewById(R.id.btn_bottom_com);
        _imgBtnBottom.setOnClickListener(v -> {
            TxtToBottom(_txtMessag);
        });
        _imgBtnClear = findViewById(R.id.btn_clear_com);
        _imgBtnClear.setOnClickListener(v -> {
            TxtClear(_txtMessag);
        });
        _btnSend = findViewById(R.id.btn_send_com);
        _btnSend.setOnClickListener(v -> {
            if (_btnSend.getText().equals("开始发送")) {
                try {
                    Log.i(TAG, "串口名称：" + _portName + "，波特率：" + _baudRate + "，发送的数据：" + _hexData);
                    MySerialPortManager.NewInstance(_portName, _baudRate);
                    _timer = new Timer();
                    _timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            InitTimerTask();
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔
                    _timer.schedule(_timerTask, 0, TASKTIME);
                    _btnSend.setText("停止发送");
                } catch (Exception e) {
                    e.printStackTrace();
                    UpdateText(_txtMessag, "\r\n" + e.toString(), "Append");
                    MyProgressDialogUtil.ShowWarning(ComTestActivity.this, "知道了", "警告", "串口打开失败，请检查串口节点是否正确！", true, new MyProgressDialogUtil.WarningListener() {
                        @Override
                        public void OnIKnow() {
                        }
                    });
                }
            } else {
                _btnSend.setText("开始发送");
                _timer.cancel();
                _timerTask.cancel();
                MySerialPortManager.Close();
            }
        });
        _btnPass.setEnabled(false);
    }

}
