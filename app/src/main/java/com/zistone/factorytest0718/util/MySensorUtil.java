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
 * 获取传感器的数据
 *
 * @author LiWei
 * @package com.zistone.factorytest0718.util
 * @fileName BleListener
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class MySensorUtil implements SensorEventListener {

    private static final String TAG = "MySensorUtil";

    private static MySensorUtil _mySensorUtil;
    private SensorManager _sensorManager;
    private MySensorListener _mySensorListener;
    //加速度传感器
    private Sensor _accelerometerSensor;
    //磁场传感器
    private Sensor _magneticSensor;
    //光照传感器
    private Sensor _lightSensor;
    private float[] _lightArray = new float[3];
    private float[] _accelerometerArray = new float[3];
    private float[] _magneticArray = new float[3];

    public interface MySensorListener {
        /**
         * 光感数据变化
         *
         * @param array
         */
        void LightChanged(float[] array);

        /**
         * 加速度数据变化
         *
         * @param array
         */
        void AccelerometerChanged(float[] array);

        /**
         * 磁场数据变化
         *
         * @param array
         */
        void MagneticChanged(float[] array);

        /**
         * 方向（旋转向量）数据变化
         *
         * @param array
         */
        void DirectionChanged(float[] array);
    }

    /**
     * 注销监听
     */
    public void UnRegister() {
        _sensorManager.unregisterListener(this, _lightSensor);
        _sensorManager.unregisterListener(this, _accelerometerSensor);
        _sensorManager.unregisterListener(this, _magneticSensor);
    }

    /**
     * 注册监听
     * <p>
     * 第三个参数是传感器数据更新的速度
     * SENSOR_DELAY_UI
     * SENSOR_DELAY_NORMAL
     * SENSOR_DELAY_GAME
     * SENSOR_DELAY_FASTEST
     */
    public void Register() {
        _sensorManager.registerListener(this, _lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        _sensorManager.registerListener(this, _accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        _sensorManager.registerListener(this, _magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public static MySensorUtil GetInstance() {
        if (null == _mySensorUtil) {
            _mySensorUtil = new MySensorUtil();
        }
        return _mySensorUtil;
    }

    public boolean Init(Context context, MySensorListener listener) {
        _sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (null != _sensorManager) {
            _mySensorListener = listener;
            List<Sensor> sensorList = _sensorManager.getSensorList(Sensor.TYPE_ALL);
            Log.i(TAG, "该设备支持的传感器：");
            for (Sensor sensor : sensorList) {
                Log.i(TAG, "名称：" + sensor.getName() + "，类型：" + sensor.getType());
                switch (sensor.getType()) {
                    //光感
                    case Sensor.TYPE_LIGHT:
                        _lightSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                        break;
                    //加速度
                    case Sensor.TYPE_ACCELEROMETER:
                        _accelerometerSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                        break;
                    //磁场
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        _magneticSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                        break;
                }
            }
            return true;
        }
        Log.e(TAG, "传感器初始化失败！");
        return false;
    }

    /**
     * 效数值存放在values[0]中
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            //光感
            case Sensor.TYPE_LIGHT: {
                _lightArray = event.values;
                Log.i(TAG, "光照强度：" + _lightArray[0]);
                _mySensorListener.LightChanged(_lightArray);
            }
            break;
            //加速度
            case Sensor.TYPE_ACCELEROMETER: {
                _accelerometerArray = event.values;
                float x = _accelerometerArray[0];
                float y = _accelerometerArray[1];
                float z = _accelerometerArray[2];
                Log.i(TAG, "加速度，X： " + x + "，Y： " + y + "，Z： " + z);
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
                _mySensorListener.AccelerometerChanged(_accelerometerArray);
            }
            break;
            //磁场
            case Sensor.TYPE_MAGNETIC_FIELD: {
                _magneticArray = event.values;
                float x = _magneticArray[0];
                float y = _magneticArray[1];
                float z = _magneticArray[2];
                Log.i(TAG, "磁场方向，X： " + x + "，Y： " + y + "，Z： " + z);
                _mySensorListener.MagneticChanged(_magneticArray);
            }
            break;
        }
        //方向（旋转向量），根据旋转矩阵、加速度、磁场数据计算而来
        if (null != _accelerometerArray && _accelerometerArray.length > 0 && null != _magneticArray && _magneticArray.length > 0) {
            //方向数据
            float[] directionArray = new float[3];
            //旋转矩阵
            float[] tempArray = new float[9];
            //更新旋转矩阵
            SensorManager.getRotationMatrix(tempArray, null, _accelerometerArray, _magneticArray);
            //根据旋转矩阵计算方向
            SensorManager.getOrientation(tempArray, directionArray);
            float x = directionArray[0];
            float y = directionArray[1];
            float z = directionArray[2];
            Log.i(TAG, "方向（旋转向量），X： " + x + "，Y： " + y + "，Z： " + z);
            _mySensorListener.DirectionChanged(directionArray);
        }
    }

    /**
     * accuracy分为0（unreliable），1（low），2（medium），3（high）
     * 注意：0并不代表有问题，是传感器需要校准
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
