package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyLightSensorUtil;

public class SensorActivity extends BaseActivity {

    private static final String TAG = "SensorActivity";

    private MyLightSensorUtil _myLightSensorUtil;
    private MyLightSensorUtil.SensorListener _sensorListener;
    private TextView _txtLight, _txtBattery, _txtSpeed, _txtMagnetic, _txtRotate;
    private IntentFilter _batteryIntentFilter;

    private BroadcastReceiver _batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //电池剩余电量
            int value1 = intent.getIntExtra("level", 0);
            //获取电池满电量数值
            int value2 = intent.getIntExtra("scale", 0);
            //获取电池技术支持
            String str = intent.getStringExtra("technology");
            //获取电池状态
            int value4 = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
            //获取电源信息
            int value5 = intent.getIntExtra("plugged", 0);
            //获取电池健康度
            int value6 = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
            //获取电池电压
            int value7 = intent.getIntExtra("voltage", 0);
            //获取电池温度
            int value8 = intent.getIntExtra("temperature", 0);
            _txtBattery.setText("电量：" + value1 + "%\n温度：" + (double) value8 / 10 + "℃");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销光感
        _myLightSensorUtil.UnRegisterSensor();
        //注销电池
        unregisterReceiver(_batteryBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册光感
        _myLightSensorUtil.RegisterSensor();
        //注册电池
        registerReceiver(_batteryBroadcastReceiver, _batteryIntentFilter);
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
        //光感
        if (_myLightSensorUtil.Init(getApplicationContext())) {
            Log.i(TAG, "已检测到该设备的光传感器");
            _myLightSensorUtil.SetSensorListener(_sensorListener);
        } else {
            Log.e(TAG, "未检测到该设备的光传感器");
        }
        //电池
        _batteryIntentFilter = new IntentFilter();
        _batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(_batteryBroadcastReceiver, _batteryIntentFilter);
        //
    }
}
