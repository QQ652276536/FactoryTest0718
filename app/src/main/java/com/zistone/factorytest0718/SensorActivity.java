package com.zistone.factorytest0718;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MySensorUtil;

/**
 * 传感器信息
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class SensorActivity extends BaseActivity {

    private static final String TAG = "SensorActivity";

    private MySensorUtil _mySensorUtil;
    private MySensorUtil.MySensorListener _mySensorListener;
    private TextView _txtLight, _txtBattery, _txtAccelerometer, _txtMagnetic, _txtRotate;
    private IntentFilter _batteryIntentFilter;

    private BroadcastReceiver _batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equalsIgnoreCase(Intent.ACTION_BATTERY_CHANGED)) {
                //电池剩余电量
                int value1 = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                //获取电池满电量数值
                int value2 = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                //获取电池技术支持
                String str = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                //获取电池状态
                int value4 = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                //获取电源信息
                int value5 = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                String batteryState = "未充电";
                switch (value5) {
                    //充电状态
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        batteryState = "充电中";
                        break;
                    //放电中
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        batteryState = "放电中";
                        break;
                    //未充电
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        batteryState = "未充电";
                        break;
                    //电池满
                    case BatteryManager.BATTERY_STATUS_FULL:
                        batteryState = "电池满";
                        break;
                }
                //获取电池健康度
                int value6 = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
                //获取电池电压
                int value7 = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                //获取电池温度
                int value8 = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                _txtBattery.setText("电量：" + value1 + "%\n温度：" + (double) value8 / 10 + "℃\n状态：" + batteryState);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销传感器
        _mySensorUtil.UnRegister();
        //注销电池
        unregisterReceiver(_batteryBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册传感器
        _mySensorUtil.Register();
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
        _txtAccelerometer = findViewById(R.id.txt_accelerometer_sensor);
        _txtMagnetic = findViewById(R.id.txt_magnetic_sensor);
        _txtRotate = findViewById(R.id.txt_rotate_sensor);
        _mySensorListener = new MySensorUtil.MySensorListener() {
            @Override
            public void LightChanged(float[] array) {
                _txtLight.setText((int) array[0] + "lx");
            }

            @Override
            public void AccelerometerChanged(float[] array) {
                String x = String.format("%.2f", array[0]);
                String y = String.format("%.2f", array[1]);
                String z = String.format("%.2f", array[2]);
                _txtAccelerometer.setText(x + "米/秒²\n" + y + "米/秒²\n" + z + "米/秒²");
            }

            @Override
            public void MagneticChanged(float[] array) {
                String x = String.format("%.2f", array[0]);
                String y = String.format("%.2f", array[1]);
                String z = String.format("%.2f", array[2]);
                _txtMagnetic.setText(x + "μT\n" + y + "μT\n" + z + "μT");
            }

            @Override
            public void DirectionChanged(float[] array) {
                String x = String.format("%.2f", array[0]);
                String y = String.format("%.2f", array[1]);
                String z = String.format("%.2f", array[2]);
                _txtRotate.setText(x + "\n" + y + "\n" + z);
            }
        };
        //电池
        _batteryIntentFilter = new IntentFilter();
        _batteryIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(_batteryBroadcastReceiver, _batteryIntentFilter);
        //传感器
        _mySensorUtil = MySensorUtil.GetInstance();
        _mySensorUtil.Init(this, _mySensorListener);
    }
}
