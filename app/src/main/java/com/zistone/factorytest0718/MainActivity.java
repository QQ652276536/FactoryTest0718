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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private boolean _isPermissionRequested = false;
    private Button _btnBluetooth, _btnWifi, _btnGPS, _btnKeyDown, _btnSIM, _btnScreen, _btnSound, _btnCOM, _btnTouch, _btnIdCard, _btnWaterCamera, _btnSystemCamera, _btnNFC, _btnScanCode, _btnBankCard, _btnFaceIdCompare;
    //记录点击返回键的时间
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_bluetooth:
                startActivity(new Intent(this, BluetoothActivity.class));
                break;
            case R.id.btn_wifi:
                startActivity(new Intent(this, WifiActivity.class));
                break;
            case R.id.btn_gps:
                startActivity(new Intent(this, GpsActivity.class));
                break;
            case R.id.btn_keydown:
                startActivity(new Intent(this, KeyDownActivity.class));
                break;
            case R.id.btn_sim:
                startActivity(new Intent(this, SimActivity.class));
                break;
            case R.id.btn_screen:
                startActivity(new Intent(this, ScreenActivity.class));
                break;
            case R.id.btn_sound:
                startActivity(new Intent(this, SoundActivity.class));
                break;
            case R.id.btn_com:
                startActivity(new Intent(this, ComTestActivity.class));
                break;
            case R.id.btn_touch:
                startActivity(new Intent(this, TouchActivity.class));
                break;
            case R.id.btn_idcard:
                startActivity(new Intent(this, IdCardActivity.class));
                break;
            case R.id.btn_gpscamera:
                startActivity(new Intent(this, WatermarkCameraActivity.class));
                break;
            case R.id.btn_systemcamera:
                startActivity(new Intent(this, SystemCameraActivity.class));
                break;
            case R.id.btn_nfc:
                startActivity(new Intent(this, NfcActivity.class));
                break;
            case R.id.btn_scancode:
                Intent intent = new Intent(this, ScanCodeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("key", "");
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.btn_bankcard:
                startActivity(new Intent(this, BankCardActivity.class));
                break;
            case R.id.btn_faceidcompare:
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
        _btnFaceIdCompare = findViewById(R.id.btn_faceidcompare);
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
        _btnFaceIdCompare.setOnClickListener(this::onClick);
    }

}
