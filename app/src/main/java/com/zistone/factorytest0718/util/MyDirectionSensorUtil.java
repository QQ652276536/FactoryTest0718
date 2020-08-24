package com.zistone.factorytest0718.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

/**
 * 方向传感器
 */
public class MyDirectionSensorUtil implements SensorEventListener {

    private static final String TAG = "MyDirectionSensorUtil";
    private static final Object _object = new Object();
    private static MyDirectionSensorUtil _myDirectionSensorUtil;
    private static Context _context;

    private static SensorManager _sensorManager;
    private static List<Sensor> _list;
    private SensorListener _sensorListener;
    //加速度传感器
    private Sensor _accelerometerSensor;
    //磁场传感器
    private Sensor _magneticSensor;
    //设备是否有加速度传感器、磁场传感器
    private static boolean _isContainsAccelerometer = false, _isContainsMagnetic = false;
    private float[] _accelerometerArray = new float[3];
    private float[] _magneticArray = new float[3];

    public interface SensorListener {
        void SensorChanged(float[] array);
    }

    /**
     * （禁止外部实例化）
     */
    private MyDirectionSensorUtil() {
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
                _isContainsAccelerometer = true;
                _accelerometerSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            } else if (Sensor.TYPE_MAGNETIC_FIELD == sensor.getType()) {
                _isContainsMagnetic = true;
                _magneticSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }
        }
        return _isContainsAccelerometer && _isContainsMagnetic;
    }

    public static MyDirectionSensorUtil GetInstance() {
        if (_myDirectionSensorUtil == null) {
            synchronized (_object) {
                if (_myDirectionSensorUtil == null) {
                    _myDirectionSensorUtil = new MyDirectionSensorUtil();
                }
            }
        }
        return _myDirectionSensorUtil;
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
        if (_isContainsAccelerometer) {
            _sensorManager.registerListener(this, _accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.i(TAG, "加速度传感器注册成功");
        } else {
            Log.i(TAG, "加速度传感器注册失败");
        }
        if (_isContainsMagnetic) {
            _sensorManager.registerListener(this, _magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.i(TAG, "磁场传感器注册成功");
        } else {
            Log.i(TAG, "磁场传感器注册失败");
        }
    }

    public void UnRegisterSensor() {
        if (null != _sensorManager && _isContainsAccelerometer || null != _sensorManager && _isContainsMagnetic) {
            _sensorManager.unregisterListener(this);
        }
    }

    /**
     * 对于方向传感器，有效数值存放在values[0]中
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        //方向传感器
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            _accelerometerArray = event.values;
        }
        //磁场传感器
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            _magneticArray = event.values;
        }
        //方向数据
        float[] tempArray1 = new float[3];
        //旋转矩阵
        float[] tempArray2 = new float[9];
        //更新旋转矩阵
        SensorManager.getRotationMatrix(tempArray2, null, _accelerometerArray, _magneticArray);
        //根据旋转矩阵计算方向
        SensorManager.getOrientation(tempArray2, tempArray1);
        float x = tempArray1[0];
        float y = tempArray1[1];
        float z = tempArray1[2];
        Log.i(TAG, "X方向： " + x + "，Y方向： " + y + "，Z方向： " + z);
        _sensorListener.SensorChanged(tempArray1);
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

