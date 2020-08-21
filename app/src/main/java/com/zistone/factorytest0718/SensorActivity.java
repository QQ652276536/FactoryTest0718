package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyLightSensorUtil;

public class SensorActivity extends BaseActivity {

    private static final String TAG = "SensorActivity";

    private MyLightSensorUtil _myLightSensorUtil;
    private MyLightSensorUtil.SensorListener _sensorListener;
    private TextView _txtLight, _txtBattery, _txtSpeed, _txtMagnetic, _txtRotate;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _myLightSensorUtil.UnRegisterSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _myLightSensorUtil.RegisterSensor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_sensor);
        SetBaseContentView(R.layout.activity_sensor);
        _txtLight = findViewById(R.id.txt_light_sensor);
        _txtBattery = findViewById(R.id.txt_battery_sensor);
        _txtSpeed = findViewById(R.id.txt_speed_sensor);
        _txtMagnetic = findViewById(R.id.txt_magnetic_sensor);
        _txtRotate = findViewById(R.id.txt_rotate_sensor);
        _myLightSensorUtil = MyLightSensorUtil.GetInstance();
        _sensorListener = value -> {
            _txtLight.setText(value + "lx");
        };
        if (_myLightSensorUtil.Init(getApplicationContext())) {
            Log.i(TAG, "已检测到该设备的光传感器");
            _myLightSensorUtil.SetSensorListener(_sensorListener);
        } else {
            Log.e(TAG, "未检测到该设备的光传感器");
        }
    }
}
