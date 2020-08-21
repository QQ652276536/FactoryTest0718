package com.zistone.factorytest0718;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.factorytest0718.util.MyConvertUtil;
import com.zistone.factorytest0718.util.MySoundPlayUtil;
import com.zistone.gpio.Gpio;
import com.zz.api.CardDriverAPI;
import com.zz.impl.cpucard.CPUCardDeviceImpl;
import com.zz.impl.idcard.IDCardDeviceImpl;
import com.zz.impl.idcard.IDCardInterface;
import com.zz.impl.mifarecard.MifareCardDeviceImpl;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class IdCardActivity extends BaseActivity {

    private static final String TAG = "IdCardActivity";
    private static final String PORT_NAME = "/dev/ttyHSL2";
    private static final int BAUDRATE = 115200;
    //读卡成功
    private static final int READ_CARD_SUCCESS = 100;
    //读卡失败
    private static final int READ_CARD_FAIL = 101;
    //刷新界面信息
    private static final int READ_CARD_THREAD_EXIT = 102;

    private static volatile boolean _threadExitFlag = true;
    private static volatile boolean _readingFlag = false;
    private static volatile boolean bFingerFlag = false;

    private boolean _isAppOnForeground = true;
    private TextView _txt;
    private ImageView _imageView;
    private Button _btnRead, _btnVersion;
    private ImageButton _btnTop, _btnBottom, _btnClear;
    private Handler _handler;
    private byte[] _message = new byte[100];
    private IDCardInterface _idCardInterface;
    private ReadIDCardThread _readIDCardThread;
    private Gpio _gpio = Gpio.getInstance();

    private class ReadIDCardThread extends Thread {
        @Override
        public void run() {
            synchronized (this) {
                while (!_threadExitFlag) {
                    if (_readingFlag) {
                        continue;
                    }
                    Message msg = new Message();
                    Message thread_msg = new Message();

                    _readingFlag = true;
                    int result = 0;
                    try {
                        //设置超时时间为5秒
                        int timeOut = 5;
                        result = _idCardInterface.readIDCard(PORT_NAME, BAUDRATE, bFingerFlag, timeOut, _message);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    if (result != 0) {
                        _readingFlag = false;
                        try {
                            String str = new String(_message, "GBK");
                            msg.what = READ_CARD_FAIL;
                            msg.obj = String.format("错误信息:%s", str);
                            _handler.sendMessage(msg);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        _readingFlag = false;
                        msg.what = READ_CARD_SUCCESS;
                        _handler.sendMessage(msg);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (_threadExitFlag) {
                        thread_msg.what = READ_CARD_THREAD_EXIT;
                        _handler.sendMessage(thread_msg);
                    }
                }
            }
        }
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

    @SuppressLint("HandlerLeak")
    private void InitHandlerMessage() {
        // 初始化handle，绑定在主线程中的队列消息中
        _handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //读卡成功
                if (msg.what == READ_CARD_SUCCESS) {
                    _btnPass.setEnabled(true);
                    ShowIdCardInfo();
                    //读取成功后停止循环读取，并发出提示音
                    _threadExitFlag = true;
                    MySoundPlayUtil.SystemSoundPlay(IdCardActivity.this);
                    _btnRead.setEnabled(true);
                    _btnVersion.setEnabled(true);
                    _btnRead.setText("读取身份证");
                }
                //读卡失败
                else if (msg.what == READ_CARD_FAIL) {
                    UpdateText(msg.obj.toString(), "Append");
                    _imageView.setImageDrawable(getDrawable(R.drawable.idcard_background));
                    _imageView.invalidate();
                }
                //等待读卡完成
                else if (msg.what == READ_CARD_THREAD_EXIT) {
                    UpdateText("本次读卡结束", "Append");
                    _btnRead.setEnabled(true);
                    _btnVersion.setEnabled(true);
                    _btnRead.setText("读取身份证");
                }
            }
        };
    }

    private void UpdateText(final String str, final String setOrAppend) {
        if (null == _txt)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (setOrAppend) {
                    case "Set":
                        _txt.setText(str);
                        break;
                    case "Append":
                        _txt.append(str + "\n");
                        TxtToBottom(_txt);
                        break;
                }
            }
        });
    }

    public void ShowIdCardInfo() {
        if (_idCardInterface.getIDCardType() == 0) {
            String txt = "姓名：" + _idCardInterface.getName() + "	性别:" + _idCardInterface.getSex() + "\r\n";
            txt += "民族：" + _idCardInterface.getNation() + "	出生日期：" + _idCardInterface.getBorn() + "\r\n";
            txt += "住址：" + _idCardInterface.getAddress() + "\r\n";
            txt += "身份证号码：" + _idCardInterface.getIdNumber() + "\r\n";
            txt += "签发机关：" + _idCardInterface.getIssueOffice() + "\r\n";
            txt += "有效期限:" + _idCardInterface.getBeginDate() + "-" + _idCardInterface.getEndDate() + "\r\n";
            UpdateText(txt, "Append");
        } else if (_idCardInterface.getIDCardType() == 1) {
            String txt = "英文姓名：" + _idCardInterface.getEnglishName() + "	性别:" + _idCardInterface.getSex() + "\r\n";
            txt += "永久居留证号码：" + _idCardInterface.getIdNumber() + "\r\n";
            txt += "国籍及所在地区代码：" + _idCardInterface.getAreaCode() + "\r\n";
            txt += "中文姓名：" + _idCardInterface.getName() + "\r\n";
            txt += "出生日期：" + _idCardInterface.getBorn() + "\r\n";
            txt += "证件版本号：" + _idCardInterface.getCardVersionNum() + "\r\n";
            txt += "当次申请受理机关代码:" + _idCardInterface.getIssueOffice() + "\r\n";
            txt += "有效期限:" + _idCardInterface.getBeginDate() + "-" + _idCardInterface.getEndDate() + "\r\n";
            UpdateText(txt, "Append");
        } else if (_idCardInterface.getIDCardType() == 2) {
            String txt = "姓名：" + _idCardInterface.getName() + "	性别:" + _idCardInterface.getSex() + "\r\n";
            txt += "出生日期：" + _idCardInterface.getBorn() + "\r\n";
            txt += "住址：" + _idCardInterface.getAddress() + "\r\n";
            txt += "身份证号码：" + _idCardInterface.getIdNumber() + "\r\n";
            txt += "签发机关：" + _idCardInterface.getIssueOffice() + "\r\n";
            txt += "有效期限:" + _idCardInterface.getBeginDate() + "-" + _idCardInterface.getEndDate() + "\r\n";
            txt += "签发次数：" + _idCardInterface.getIssueCount() + "\r\n";
            txt += "通行证号码：" + _idCardInterface.getPassportNum() + "\r\n";
            UpdateText(txt, "Append");
        } else {
            UpdateText("证件类型状态非法", "Append");
        }
        if (bFingerFlag) {
            if (_idCardInterface.getFingerLen() == 0) {
                UpdateText("卡片内无指纹", "Append");
            } else {
                UpdateText("卡片内有指纹", "Append");
            }
        } else {
            UpdateText("未读指纹数据", "Append");
        }
        Bitmap bitmap = _idCardInterface.getPhotoBmp();
        _imageView.setImageBitmap(bitmap);
    }

    public void OnClickReadIDCard(View view) {
        if (_btnRead.getText().equals("读取身份证")) {
            _btnVersion.setEnabled(false);
            _threadExitFlag = false;
            _btnRead.setText("停 止");
            _readIDCardThread = new ReadIDCardThread();
            _readIDCardThread.start();
        } else {
            _threadExitFlag = true;
            // 停止读卡时，先禁用按钮，等待当前一次读卡结束后再启用按钮
            _btnRead.setEnabled(false);
            UpdateText("正在等待本次读卡结束，请稍候...", "Append");
        }
    }

    String getErrorDesc(int errCode, byte[] errBuf) {
        String errMsg = new String(errBuf).trim();
        switch (errCode) {
            case CardDriverAPI.ERR_CARD_SW3_WRONG:
                return "错误码:" + errCode + ",错误描述：非接卡应答结果错误," + errMsg;
            case CardDriverAPI.ERR_CARD_SW3_MULTICARD:
                return "错误码:" + errCode + ",错误描述：射频区域有多张卡重叠," + errMsg;
            case CardDriverAPI.ERR_FIND_CARD_TIMEOUT:
                return "错误码:" + errCode + ",错误描述：等待放卡超时," + errMsg;
            case CardDriverAPI.ERR_PARAM_INVALID:
                return "错误码:" + errCode + ",错误描述：参数非法," + errMsg;
            case CardDriverAPI.ERR_PORT_OPEN_FAIL:
                return "错误码:" + errCode + ",错误描述：打开串口失败," + errMsg;
            case CardDriverAPI.ERR_SEND_DATA_FAIL:
                return "错误码:" + errCode + ",错误描述：发送数据失败," + errMsg;
            case CardDriverAPI.ERR_WAIT_TIME_OUT:
                return "错误码:" + errCode + ",错误描述：等待应答超时," + errMsg;
            case CardDriverAPI.ERR_RECV_DATA_FAIL:
                return "错误码:" + errCode + ",错误描述：接收应答失败," + errMsg;
            default:
                return "错误码:" + errCode + ",错误描述：未知错误," + errMsg;
        }
    }

    public void OnClickReadVersion(View view) throws Exception {
        int result = 0;
        byte[] _message = new byte[201];
        byte[] samVersion = new byte[201];

        result = _idCardInterface.getSAMVersion(PORT_NAME, BAUDRATE, samVersion, _message);
        if (result == 0) {
            UpdateText("SAM版本号：" + new String(samVersion), "Append");
        } else {
            UpdateText("读取SAM版本失败," + getErrorDesc(result, _message), "Append");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        _isAppOnForeground = IsAppOnForeground();
        if (!_isAppOnForeground) {
            _gpio.set_gpio(0, 66);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        _gpio.set_gpio(1, 66);
        if (!_isAppOnForeground)
            _isAppOnForeground = true;
    }

    @Override
    protected void onDestroy() {
        _gpio.set_gpio(0, 66);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_idcard);
        SetBaseContentView(R.layout.activity_idcard);
        _txt = findViewById(R.id.text_idcard);
        _txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        _imageView = findViewById(R.id.iv_idcard);
        _btnRead = findViewById(R.id.btn_read_idcard);
        _btnVersion = findViewById(R.id.btn_readversion_idcard);
        _btnTop = findViewById(R.id.btn_top_idcard);
        _btnTop.setOnClickListener(v -> TxtToTop(_txt));
        _btnBottom = findViewById(R.id.btn_bottom_idcard);
        _btnBottom.setOnClickListener(v -> TxtToBottom(_txt));
        _btnClear = findViewById(R.id.btn_clear_idcard);
        _btnClear.setOnClickListener(v -> TxtClear(_txt));
        _btnRead.setFocusable(true);
        _btnRead.setFocusableInTouchMode(true);
        _btnRead.requestFocus();
        _btnRead.requestFocusFromTouch();
        _idCardInterface = new IDCardDeviceImpl();
        InitHandlerMessage();
        _gpio.set_gpio(1, 66);
        _btnPass.setOnClickListener(v -> {
            _gpio.set_gpio(0, 66);
            Pass();
        });
        _btnFail.setOnClickListener(v -> {
            _gpio.set_gpio(0, 66);
            Fail();
        });
        _btnPass.setEnabled(false);
    }

}
