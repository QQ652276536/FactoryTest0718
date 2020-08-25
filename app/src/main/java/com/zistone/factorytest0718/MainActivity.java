package com.zistone.factorytest0718;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zistone.factorytest0718.util.MyActivityManager;
import com.zistone.factorytest0718.util.MySharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int BLUETOOTH_ACTIVITY_CODE = 101;
    private static final int WIFI_ACTIVITY_CODE = 102;
    private static final int GPS_ACTIVITY_CODE = 103;
    private static final int KEYDOWN_ACTIVITY_CODE = 104;
    private static final int SIM_ACTIVITY_CODE = 105;
    private static final int SCREEN_ACTIVITY_CODE = 106;
    private static final int SOUND_ACTIVITY_CODE = 107;
    private static final int SCMTEST_ACTIVITY_CODE = 108;
    private static final int TOUCH_ACTIVITY_CODE = 109;
    private static final int IDCARD_ACTIVITY_CODE = 110;
    private static final int WATERMARKCAMERA_ACTIVITY_CODE = 111;
    private static final int SYSTEMCAMERA_ACTIVITY_CODE = 112;
    private static final int NFCACTIVITY_CODE = 113;
    private static final int SCANCODE_ACTIVITY_CODE = 114;
    private static final int BANKCARD_ACTIVITY_CODE = 115;
    private static final int FACE_ACTIVITY_CODE = 116;
    private static final int TFCARD_ACTIVITY_CODE = 117;
    private static final int SENSOR_ACTIVITY_CODE = 118;
    private static final int SHAKE_ACTIVITY_CODE = 119;

    private boolean _isPermissionRequested = false;
    private Button _btnBluetooth, _btnWifi, _btnGPS, _btnKeyDown, _btnSIM, _btnScreen, _btnSound, _btnSCM, _btnTouch, _btnIdCard, _btnWaterCamera, _btnSystemCamera, _btnNFC, _btnScanCode, _btnBankCard, _btnTestTest, _btnFace, _btnTfCard, _btnSensor, _btnShake;
    private long _exitTime = 0;
    private Map<Integer, Boolean> _testResultMap;
    private Map<Integer, Button> _testBtnMap;

    /**
     * Android6.0之后需要动态申请权限
     */
    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !_isPermissionRequested) {
            _isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {Manifest.permission.WRITE_SETTINGS, Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE,
                                    Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.WAKE_LOCK,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CAMERA,
                                    Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.READ_SMS, Manifest.permission.RECORD_AUDIO};
            for (String perm : permissions) {
                //进入到这里代表没有权限
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm))
                    permissionsList.add(perm);
            }
            if (!permissionsList.isEmpty())
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
        }
    }

    /**
     * 判断功能是否测试过
     */
    private void JudgePassFail() {
        Iterator<Map.Entry<Integer, Boolean>> iterator1 = _testResultMap.entrySet().iterator();
        //所有功能的测试结果
        while (iterator1.hasNext()) {
            Map.Entry<Integer, Boolean> entry1 = iterator1.next();
            int key1 = entry1.getKey();
            //所有功能对应的按钮
            Iterator<Map.Entry<Integer, Button>> iterator2 = _testBtnMap.entrySet().iterator();
            while (iterator2.hasNext()) {
                Map.Entry<Integer, Button> entry2 = iterator2.next();
                int key2 = entry2.getKey();
                if (key1 == key2) {
                    if (null == entry1.getValue() || !entry1.getValue()) {
                        SetPassBackgroundColor(entry2.getValue(), FAIL);
                    } else {
                        SetPassBackgroundColor(entry2.getValue(), PASS);
                    }
                    break;
                }
            }
        }
    }

    /**
     * 根据设备类型决定展现（隐藏）哪些测试功能
     */
    private void JudgeDeviceType() {
        if (_deviceType.contains("wd220B")) {
            //测试用的Activity
            _btnTestTest.setVisibility(View.GONE);
            //水印相机
            _btnWaterCamera.setVisibility(View.GONE);
            //人脸识别
            _btnFace.setVisibility(View.GONE);
            //银行卡读取（使用浙江中正的身份证模块）
            _btnBankCard.setVisibility(View.GONE);
        }
    }

    private void SetPassBackgroundColor(Button btn, String result) {
        if (PASS.equals(result))
            btn.setBackground(getDrawable(R.drawable.main_btn_background2));
        else
            btn.setBackground(getDrawable(R.drawable.main_btn_background1));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - _exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                _exitTime = System.currentTimeMillis();
            } else {
                MyActivityManager.ExitAPP();
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == data) {
            return;
        }
        if (resultCode == RESULT_OK) {
            String str = data.getStringExtra(ARG_PARAM1);
            Log.i(TAG, "返回时携带的数据：" + str);
            switch (requestCode) {
                case BLUETOOTH_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnBluetooth, str);
                    _testResultMap.put(BLUETOOTH_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case WIFI_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnWifi, str);
                    _testResultMap.put(WIFI_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case GPS_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnGPS, str);
                    _testResultMap.put(GPS_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case KEYDOWN_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnKeyDown, str);
                    _testResultMap.put(KEYDOWN_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case SIM_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSIM, str);
                    _testResultMap.put(SIM_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case SCREEN_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnScreen, str);
                    _testResultMap.put(SCREEN_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case SOUND_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSound, str);
                    _testResultMap.put(SOUND_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case SCMTEST_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSCM, str);
                    _testResultMap.put(SCMTEST_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case TOUCH_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnTouch, str);
                    _testResultMap.put(TOUCH_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case IDCARD_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnIdCard, str);
                    _testResultMap.put(IDCARD_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case WATERMARKCAMERA_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnWaterCamera, str);
                    _testResultMap.put(WATERMARKCAMERA_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case SYSTEMCAMERA_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSystemCamera, str);
                    _testResultMap.put(SYSTEMCAMERA_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case NFCACTIVITY_CODE:
                    SetPassBackgroundColor(_btnNFC, str);
                    _testResultMap.put(NFCACTIVITY_CODE, str.equals(PASS));
                    break;
                case SCANCODE_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnScanCode, str);
                    _testResultMap.put(SCANCODE_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case BANKCARD_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnBankCard, str);
                    _testResultMap.put(BANKCARD_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case FACE_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnFace, str);
                    _testResultMap.put(FACE_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case TFCARD_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnTfCard, str);
                    _testResultMap.put(TFCARD_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case SENSOR_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSensor, str);
                    _testResultMap.put(SENSOR_ACTIVITY_CODE, str.equals(PASS));
                    break;
                case SHAKE_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnShake, str);
                    _testResultMap.put(SHAKE_ACTIVITY_CODE, str.equals(PASS));
                    break;
            }
            MySharedPreferences.SetMainPassFail(this, _testResultMap);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bluetooth:
                startActivityForResult(new Intent(this, BluetoothActivity.class), BLUETOOTH_ACTIVITY_CODE);
                break;
            case R.id.btn_wifi:
                startActivityForResult(new Intent(this, WifiActivity.class), WIFI_ACTIVITY_CODE);
                break;
            case R.id.btn_gps:
                startActivityForResult(new Intent(this, GpsActivity.class), GPS_ACTIVITY_CODE);
                break;
            case R.id.btn_keydown:
                startActivityForResult(new Intent(this, KeyDownActivity.class), KEYDOWN_ACTIVITY_CODE);
                break;
            case R.id.btn_sim:
                startActivityForResult(new Intent(this, SimActivity.class), SIM_ACTIVITY_CODE);
                break;
            case R.id.btn_screen:
                startActivityForResult(new Intent(this, ScreenActivity.class), SCREEN_ACTIVITY_CODE);
                break;
            case R.id.btn_sound:
                startActivityForResult(new Intent(this, SoundActivity.class), SOUND_ACTIVITY_CODE);
                break;
            case R.id.btn_scm:
                startActivityForResult(new Intent(this, ScmTestActivity.class), SCMTEST_ACTIVITY_CODE);
                break;
            case R.id.btn_touch:
                startActivityForResult(new Intent(this, TouchActivity.class), TOUCH_ACTIVITY_CODE);
                break;
            case R.id.btn_idcard:
                startActivityForResult(new Intent(this, IdCardActivity.class), IDCARD_ACTIVITY_CODE);
                break;
            case R.id.btn_gpscamera:
                startActivityForResult(new Intent(this, WatermarkCameraActivity.class), WATERMARKCAMERA_ACTIVITY_CODE);
                break;
            case R.id.btn_systemcamera:
                startActivityForResult(new Intent(this, SystemCameraActivity.class), SYSTEMCAMERA_ACTIVITY_CODE);
                break;
            case R.id.btn_nfc:
                startActivityForResult(new Intent(this, NfcActivity.class), NFCACTIVITY_CODE);
                break;
            case R.id.btn_scancode:
                Intent intent = new Intent(this, ScanCodeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("key", "");
                intent.putExtras(bundle);
                startActivityForResult(intent, SCANCODE_ACTIVITY_CODE);
                break;
            case R.id.btn_bankcard:
                startActivityForResult(new Intent(this, BankCardActivity.class), BANKCARD_ACTIVITY_CODE);
                break;
            case R.id.btn_face:
                startActivityForResult(new Intent(this, FaceAttributeMenuActivity.class), FACE_ACTIVITY_CODE);
                break;
            case R.id.btn_test_test:
                startActivity(new Intent(this, TestTestActivity.class));
                break;
            case R.id.btn_tfcard:
                startActivityForResult(new Intent(this, TfCardActivity.class), TFCARD_ACTIVITY_CODE);
                break;
            case R.id.btn_sensor:
                startActivityForResult(new Intent(this, SensorActivity.class), SENSOR_ACTIVITY_CODE);
                break;
            case R.id.btn_shake:
                startActivityForResult(new Intent(this, ShakeActivity.class), SHAKE_ACTIVITY_CODE);
                break;
        }
    }

    /**
     * 动态授权的回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        String content = "动态授权的回调:";
        for (int i = 0; i < permissions.length; i++) {
            content += "\r\n权限" + permissions[i] + "【" + (grantResults[i] != -1 ? "允许" : "拒绝") + "】";
        }
        Log.i(TAG, content);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JudgeDeviceType();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _deviceType = Build.MODEL;
        _systemVersion = Build.VERSION.RELEASE;
        Log.i(TAG, "\n设备型号：" + _deviceType + "，系统版本号：" + _systemVersion);
        RequestPermission();
        _btnBluetooth = findViewById(R.id.btn_bluetooth);
        _btnWifi = findViewById(R.id.btn_wifi);
        _btnGPS = findViewById(R.id.btn_gps);
        _btnKeyDown = findViewById(R.id.btn_keydown);
        _btnSIM = findViewById(R.id.btn_sim);
        _btnScreen = findViewById(R.id.btn_screen);
        _btnSound = findViewById(R.id.btn_sound);
        _btnSCM = findViewById(R.id.btn_scm);
        _btnTouch = findViewById(R.id.btn_touch);
        _btnIdCard = findViewById(R.id.btn_idcard);
        _btnWaterCamera = findViewById(R.id.btn_gpscamera);
        _btnSystemCamera = findViewById(R.id.btn_systemcamera);
        _btnNFC = findViewById(R.id.btn_nfc);
        _btnScanCode = findViewById(R.id.btn_scancode);
        _btnBankCard = findViewById(R.id.btn_bankcard);
        _btnTestTest = findViewById(R.id.btn_test_test);
        _btnFace = findViewById(R.id.btn_face);
        _btnTfCard = findViewById(R.id.btn_tfcard);
        _btnSensor = findViewById(R.id.btn_sensor);
        _btnShake = findViewById(R.id.btn_shake);
        _btnBluetooth.setOnClickListener(this::onClick);
        _btnWifi.setOnClickListener(this::onClick);
        _btnGPS.setOnClickListener(this::onClick);
        _btnKeyDown.setOnClickListener(this::onClick);
        _btnSIM.setOnClickListener(this::onClick);
        _btnScreen.setOnClickListener(this::onClick);
        _btnSound.setOnClickListener(this::onClick);
        _btnSCM.setOnClickListener(this::onClick);
        _btnTouch.setOnClickListener(this::onClick);
        _btnIdCard.setOnClickListener(this::onClick);
        _btnWaterCamera.setOnClickListener(this::onClick);
        _btnSystemCamera.setOnClickListener(this::onClick);
        _btnNFC.setOnClickListener(this::onClick);
        _btnScanCode.setOnClickListener(this::onClick);
        _btnBankCard.setOnClickListener(this::onClick);
        _btnTestTest.setOnClickListener(this::onClick);
        _btnFace.setOnClickListener(this::onClick);
        _btnTfCard.setOnClickListener(this::onClick);
        _btnSensor.setOnClickListener(this::onClick);
        _btnShake.setOnClickListener(this::onClick);
        _testBtnMap = new HashMap<Integer, Button>() {{
            put(BLUETOOTH_ACTIVITY_CODE, _btnBluetooth);
            put(WIFI_ACTIVITY_CODE, _btnWifi);
            put(GPS_ACTIVITY_CODE, _btnGPS);
            put(KEYDOWN_ACTIVITY_CODE, _btnKeyDown);
            put(SIM_ACTIVITY_CODE, _btnSIM);
            put(SCREEN_ACTIVITY_CODE, _btnScreen);
            put(SOUND_ACTIVITY_CODE, _btnSound);
            put(SCMTEST_ACTIVITY_CODE, _btnSCM);
            put(TOUCH_ACTIVITY_CODE, _btnTouch);
            put(IDCARD_ACTIVITY_CODE, _btnIdCard);
            put(WATERMARKCAMERA_ACTIVITY_CODE, _btnWaterCamera);
            put(SYSTEMCAMERA_ACTIVITY_CODE, _btnSystemCamera);
            put(NFCACTIVITY_CODE, _btnNFC);
            put(SCANCODE_ACTIVITY_CODE, _btnScanCode);
            put(BANKCARD_ACTIVITY_CODE, _btnBankCard);
            put(FACE_ACTIVITY_CODE, _btnFace);
            put(TFCARD_ACTIVITY_CODE, _btnTfCard);
            put(SENSOR_ACTIVITY_CODE, _btnSensor);
            put(SHAKE_ACTIVITY_CODE, _btnShake);
        }};
        JudgeDeviceType();
        _testResultMap = MySharedPreferences.GetMainPassFail(this);
        JudgePassFail();
    }

}
