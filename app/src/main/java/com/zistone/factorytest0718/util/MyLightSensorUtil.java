package com.zistone.factorytest0718.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

import static com.zistone.factorytest0718.BuildConfig.DEBUG;

/**
 * 光线传感器的类型常量是Sensor.TYPE_LIGHT。values数组只有第一个元素（values[0]）有意义。表示光线的强度，最大的值是120000.0f
 * Android SDK将光线强度分为不同的等级，每一个等级的最大值由一个常量表示，这些常量都定义在SensorManager类中
 * 代码如下：
 * public static final float LIGHT_SUNLIGHT_MAX =120000.0f
 * public static final float LIGHT_SUNLIGHT=110000.0f
 * public static final float LIGHT_SHADE=20000.0f
 * public static final float LIGHT_OVERCAST= 10000.0f
 * public static final float LIGHT_SUNRISE= 400.0f
 * public static final float LIGHT_CLOUDY= 100.0f
 * public static final float LIGHT_FULLMOON= 0.25f
 * public static final float LIGHT_NO_MOON= 0.001f
 * 上面的八个常量只是临界值，在实际使用光线传感器时要根据实际情况确定一个范围
 * 例如，当太阳逐渐升起时，values[0] 的值很可能会超过 LIGHT_SUNRISE
 * 当 values[0] 的值逐渐增大时，就会逐渐越过LIGHT_OVERCAST，而达到 LIGHT_SHADE
 * 当然，如果天特别好的话，也可能会达到LIGHT_SUNLIGHT，甚至更高
 */
public class MyLightSensorUtil implements SensorEventListener {

    private static final String TAG = "MyLightSensorUtil";
    private static final Object _object = new Object();
    private static MyLightSensorUtil _myLightSensorUtil;
    private static Context _context;

    private static SensorManager _sensorManager;
    private static List<Sensor> _list;
    //设备是否有光传感器
    private static boolean _isContains = false;
    private SensorListener _sensorListener;

    public interface SensorListener {
        void SensorChanged(int value);
    }

    /**
     * （禁止外部实例化）
     */
    private MyLightSensorUtil() {
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
            if (Sensor.TYPE_LIGHT == sensor.getType()) {
                _isContains = true;
                break;
            }
        }
        return _isContains;
    }

    public static MyLightSensorUtil GetInstance() {
        if (_myLightSensorUtil == null) {
            synchronized (_object) {
                if (_myLightSensorUtil == null) {
                    _myLightSensorUtil = new MyLightSensorUtil();
                }
            }
        }
        return _myLightSensorUtil;
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
            Sensor sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (null != sensor && _isContains) {
                _sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                Log.i(TAG, "光传感器注册成功");
            } else {
                Log.e(TAG, "光传感器注册失败");
            }
        }
    }

    public void UnRegisterSensor() {
        if (null != _sensorManager && _isContains) {
            _sensorManager.unregisterListener(this);
        }
    }

    /**
     * 对于光传感器，有效数值存放在values[0]中的，单位为SI lunx
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            Log.i(TAG, "当前光照强度：" + event.values[0]);
            _sensorListener.SensorChanged((int) event.values[0]);
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

