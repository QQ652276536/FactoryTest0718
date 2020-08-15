package com.zistone.factorytest0718;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int BLUETOOTH_ACTIVITY_CODE = 101;
    private static final int WIFI_ACTIVITY_CODE = 102;
    private static final int GPS_ACTIVITY_CODE = 103;
    private static final int KEYDOWN_ACTIVITY_CODE = 104;
    private static final int SIM_ACTIVITY_CODE = 105;
    private static final int SCREEN_ACTIVITY_CODE = 106;
    private static final int SOUND_ACTIVITY_CODE = 107;
    private static final int COMTEST_ACTIVITY_CODE = 108;
    private static final int TOUCH_ACTIVITY_CODE = 109;
    private static final int IDCARD_ACTIVITY_CODE = 110;
    private static final int WATERMARKCAMERA_ACTIVITY_CODE = 111;
    private static final int SYSTEMCAMERA_ACTIVITY_CODE = 112;
    private static final int NFCACTIVITY_CODE = 113;
    private static final int SCANCODE_ACTIVITY_CODE = 114;
    private static final int BANKCARD_ACTIVITY_CODE = 115;
    private static final int FACE_CODE = 116;

    private boolean _isPermissionRequested = false;
    private Button _btnBluetooth, _btnWifi, _btnGPS, _btnKeyDown, _btnSIM, _btnScreen, _btnSound, _btnCOM, _btnTouch, _btnIdCard,
            _btnWaterCamera, _btnSystemCamera, _btnNFC, _btnScanCode, _btnBankCard, _btnTestTest,_btnFace;
    private long _exitTime = 0;

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

    private void SetPassBackgroundColor(Button btn, String result) {
        if (PASS.equals(result))
            btn.setBackground(getDrawable(R.drawable.main_btn_background4));
        else
            btn.setBackground(getDrawable(R.drawable.main_btn_background5));
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
                    break;
                case WIFI_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnWifi, str);
                    break;
                case GPS_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnGPS, str);
                    break;
                case KEYDOWN_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnKeyDown, str);
                    break;
                case SIM_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSIM, str);
                    break;
                case SCREEN_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnScreen, str);
                    break;
                case SOUND_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSound, str);
                    break;
                case COMTEST_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnCOM, str);
                    break;
                case TOUCH_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnTouch, str);
                    break;
                case IDCARD_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnIdCard, str);
                    break;
                case WATERMARKCAMERA_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnWaterCamera, str);
                    break;
                case SYSTEMCAMERA_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnSystemCamera, str);
                    break;
                case NFCACTIVITY_CODE:
                    SetPassBackgroundColor(_btnNFC, str);
                    break;
                case SCANCODE_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnScanCode, str);
                    break;
                case BANKCARD_ACTIVITY_CODE:
                    SetPassBackgroundColor(_btnBankCard, str);
                    break;
                case FACE_CODE:
                    SetPassBackgroundColor(_btnFace, str);
                    break;
            }
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
            case R.id.btn_com:
                startActivityForResult(new Intent(this, ComTestActivity.class), COMTEST_ACTIVITY_CODE);
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
                startActivityForResult(new Intent(this, FaceAttributeMenuActivity.class), FACE_CODE);
                break;
            case R.id.btn_test_test:
                startActivity(new Intent(this, TestTestActivity.class));
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
        Log.i(TAG, "动态授权的回调");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestPermission();
        _btnBluetooth = findViewById(R.id.btn_bluetooth);
        _btnWifi = findViewById(R.id.btn_wifi);
        _btnGPS = findViewById(R.id.btn_gps);
        _btnKeyDown = findViewById(R.id.btn_keydown);
        _btnSIM = findViewById(R.id.btn_sim);
        _btnScreen = findViewById(R.id.btn_screen);
        _btnSound = findViewById(R.id.btn_sound);
        _btnCOM = findViewById(R.id.btn_com);
        _btnTouch = findViewById(R.id.btn_touch);
        _btnIdCard = findViewById(R.id.btn_idcard);
        _btnWaterCamera = findViewById(R.id.btn_gpscamera);
        _btnSystemCamera = findViewById(R.id.btn_systemcamera);
        _btnNFC = findViewById(R.id.btn_nfc);
        _btnScanCode = findViewById(R.id.btn_scancode);
        _btnBankCard = findViewById(R.id.btn_bankcard);
        _btnTestTest = findViewById(R.id.btn_test_test);
        _btnFace = findViewById(R.id.btn_face);
        _btnBluetooth.setOnClickListener(this::onClick);
        _btnWifi.setOnClickListener(this::onClick);
        _btnGPS.setOnClickListener(this::onClick);
        _btnKeyDown.setOnClickListener(this::onClick);
        _btnSIM.setOnClickListener(this::onClick);
        _btnScreen.setOnClickListener(this::onClick);
        _btnSound.setOnClickListener(this::onClick);
        _btnCOM.setOnClickListener(this::onClick);
        _btnTouch.setOnClickListener(this::onClick);
        _btnIdCard.setOnClickListener(this::onClick);
        _btnWaterCamera.setOnClickListener(this::onClick);
        _btnSystemCamera.setOnClickListener(this::onClick);
        _btnNFC.setOnClickListener(this::onClick);
        _btnScanCode.setOnClickListener(this::onClick);
        _btnBankCard.setOnClickListener(this::onClick);
        _btnTestTest.setOnClickListener(this::onClick);
        _btnFace.setOnClickListener(this::onClick);
    }

}
