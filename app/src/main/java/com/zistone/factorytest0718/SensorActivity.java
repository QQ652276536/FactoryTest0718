package com.zistone.factorytest0718;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyAccelerometerSensorUtil;
import com.zistone.factorytest0718.util.MyDirectionSensorUtil;
import com.zistone.factorytest0718.util.MyLightSensorUtil;
import com.zistone.factorytest0718.util.MyMagneticSensorUtil;

public class SensorActivity extends BaseActivity {

    private static final String TAG = "SensorActivity";

    private MyLightSensorUtil _myLightSensorUtil;
    private MyLightSensorUtil.SensorListener _lightSensorListener;
    private MyAccelerometerSensorUtil _myAccelerometerSensorUtil;
    private MyAccelerometerSensorUtil.SensorListener _accelerometerSensorListener;
    private MyMagneticSensorUtil _myMagneticSensorUtil;
    private MyMagneticSensorUtil.SensorListener _magneticSensorListener;
    private MyDirectionSensorUtil _myDirectionSensorUtil;
    private MyDirectionSensorUtil.SensorListener _directionListener;
    private TextView _txtLight, _txtBattery, _txtAccelerometer, _txtMagnetic, _txtRotate;
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
        //注销加速度
        _myAccelerometerSensorUtil.UnRegisterSensor();
        //注销磁场
        _myMagneticSensorUtil.UnRegisterSensor();
        //注销方向
        _myDirectionSensorUtil.UnRegisterSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册光感
        _myLightSensorUtil.RegisterSensor();
        //注册电池
        registerReceiver(_batteryBroadcastReceiver, _batteryIntentFilter);
        //注册加速度
        _myAccelerometerSensorUtil.RegisterSensor();
        //注册磁场
        _myMagneticSensorUtil.RegisterSensor();
        //注册方向
        _myDirectionSensorUtil.RegisterSensor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_sensor);
        SetBaseContentView(R.layout.activity_sensor);
        _txtLight = findViewById(R.id.txt_light_sensor);
        _txtBattery = findViewById(R.id.txt_battery_sensor);
        _txtAccelerometer = findViewById(R.id.txt_accelerometer_sensor);
        _txtMagnetic = findViewById(R.id.txt_magnetic_sensor);
        _txtRotate = findViewById(R.id.txt_rotate_sensor);
        //光感监听
        _lightSensorListener = value -> _txtLight.setText(value + "lx");
        //加速度监听
        _accelerometerSensorListener = array -> {
            String x = String.format("%.2f", array[0]);
            String y = String.format("%.2f", array[1]);
            String z = String.format("%.2f", array[2]);
            _txtAccelerometer.setText(x + "米/秒²\n" + y + "米/秒²\n" + z + "米/秒²");
        };
        //磁场传感器监听
        _magneticSensorListener = array -> {
            String x = String.format("%.2f", array[0]);
            String y = String.format("%.2f", array[1]);
            String z = String.format("%.2f", array[2]);
            _txtMagnetic.setText(x + "μT\n" + y + "μT\n" + z + "μT");
        };
        //方向传感器监听
        _directionListener = array -> {
            String x = String.format("%.2f", array[0]);
            String y = String.format("%.2f", array[1]);
            String z = String.format("%.2f", array[2]);
            _txtRotate.setText(x + "\n" + y + "\n" + z);
        };
        //光感
        _myLightSensorUtil = MyLightSensorUtil.GetInstance();
        if (_myLightSensorUtil.Init(getApplicationContext())) {
            Log.i(TAG, "已检测到该设备的光传感器");
            _myLightSensorUtil.SetSensorListener(_lightSensorListener);
        } else {
            Log.e(TAG, "未检测到该设备的光传感器");
        }
        //电池
        _batteryIntentFilter = new IntentFilter();
        _batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(_batteryBroadcastReceiver, _batteryIntentFilter);
        //加速度
        _myAccelerometerSensorUtil = MyAccelerometerSensorUtil.GetInstance();
        if (_myAccelerometerSensorUtil.Init(getApplicationContext())) {
            Log.i(TAG, "已检测到该设备的加速度传感器");
            _myAccelerometerSensorUtil.SetSensorListener(_accelerometerSensorListener);
        } else {
            Log.e(TAG, "未检测到该设备的加速度传感器");
        }
        //磁场
        _myMagneticSensorUtil = MyMagneticSensorUtil.GetInstance();
        if (_myMagneticSensorUtil.Init(getApplicationContext())) {
            Log.i(TAG, "已检测到该设备的磁场传感器");
            _myMagneticSensorUtil.SetSensorListener(_magneticSensorListener);
        } else {
            Log.e(TAG, "未检测到该设备的磁场传感器");
        }
        //方向
        _myDirectionSensorUtil = MyDirectionSensorUtil.GetInstance();
        if (_myDirectionSensorUtil.Init(getApplicationContext())) {
            Log.i(TAG, "已检测到该设备的方向传感器");
            _myDirectionSensorUtil.SetSensorListener(_directionListener);
        } else {
            Log.e(TAG, "未检测到该设备的方向传感器");
        }
    }
}
