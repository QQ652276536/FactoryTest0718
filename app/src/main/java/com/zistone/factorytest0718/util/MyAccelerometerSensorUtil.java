package com.zistone.factorytest0718.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

import static android.hardware.SensorManager.STANDARD_GRAVITY;

/**
 * 加速度计传感器
 */
public class MyAccelerometerSensorUtil implements SensorEventListener {

    private static final String TAG = "MyAccelerometerSensorUtil";
    private static final Object _object = new Object();
    private static MyAccelerometerSensorUtil _myAccelerometerSensorUtil;
    private static Context _context;

    private static SensorManager _sensorManager;
    private static List<Sensor> _list;
    //设备是否有加速度传感器
    private static boolean _isContains = false;
    private SensorListener _sensorListener;

    public interface SensorListener {
        void SensorChanged(float[] array);
    }

    /**
     * （禁止外部实例化）
     */
    private MyAccelerometerSensorUtil() {
    }

    public void SetSensorListener(SensorListener listener) {
        _sensorListener = listener;
    }

    public boolean Init(Context context) {
        _context = context;
        _sensorManager = (SensorManager) _context.getSystemService(Context.SENSOR_SERVICE);
        //获取设备上支持的传感器
        _list = _sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : _list) {
            if (Sensor.TYPE_ACCELEROMETER == sensor.getType()) {
                _isContains = true;
                break;
            }
        }
        return _isContains;
    }

    public static MyAccelerometerSensorUtil GetInstance() {
        if (_myAccelerometerSensorUtil == null) {
            synchronized (_object) {
                if (_myAccelerometerSensorUtil == null) {
                    _myAccelerometerSensorUtil = new MyAccelerometerSensorUtil();
                }
            }
        }
        return _myAccelerometerSensorUtil;
    }

    /**
     * 第三个参数是传感器数据更新数据的速度
     * 有以下四个值可选，他们的速度是递增的
     * SENSOR_DELAY_UI
     * SENSOR_DELAY_NORMAL
     * SENSOR_DELAY_GAME
     * SENSOR_DELAY_FASTEST
     */
    public void RegisterSensor() {
        if (null != _sensorManager) {
            Sensor sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (null != sensor && _isContains) {
                _sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                Log.i(TAG, "加速度传感器注册成功");
            } else {
                Log.e(TAG, "加速度传感器注册失败");
            }
        }
    }

    public void UnRegisterSensor() {
        if (null != _sensorManager && _isContains) {
            _sensorManager.unregisterListener(this);
        }
    }

    /**
     * 对于加速度传感器，有效数值存放在values[0]中
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //加速度传感器
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            Log.i(TAG, "X轴： " + x + "，Y轴： " + y + "，Z轴： " + z);
            if (x > STANDARD_GRAVITY) {
                Log.i(TAG, "重力指向设备左边");
            } else if (x < -STANDARD_GRAVITY) {
                Log.i(TAG, "重力指向设备右边");
            } else if (y > STANDARD_GRAVITY) {
                Log.i(TAG, "重力指向设备下边");
            } else if (y < -STANDARD_GRAVITY) {
                Log.i(TAG, "重力指向设备上边");
            } else if (z > STANDARD_GRAVITY) {
                Log.i(TAG, "屏幕朝上");
            } else if (z < -STANDARD_GRAVITY) {
                Log.i(TAG, "屏幕朝下");
            }
            _sensorListener.SensorChanged(event.values);
        }
    }

    /**
     * accuracy分为4档，0（unreliable），1（low），2（medium），3（high）
     * 注意0并不代表有问题，是传感器需要校准
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}

