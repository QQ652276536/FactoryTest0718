package com.zistone.factorytest0718.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;
import java.util.List;

public final class MyLocationUtil implements LocationListener {

    public interface MyLocationListener {
        void OnLocationChanged(Location location);

        void OnUpdateProviderStatus(int status);

        void OnUpdateProviders(String providers);
    }

    private static final String TAG = "MyGpsUtil";
    private static final String LOCATETYPE = LocationManager.GPS_PROVIDER;
    private static LocationManager _locationManager;
    private static WeakReference<Activity> _weakReference;
    private static MyLocationListener _myLocationListener;
    private static MyLocationUtil _myLocationUtil;

    /**
     * （禁止外部实例化）
     *
     * @param context
     * @param time               位置更新的最短时间
     * @param distance           位置更新的最小距离
     * @param myLocationListener
     */
    private MyLocationUtil(Activity context, long time, float distance, MyLocationListener myLocationListener) {
        //注册地理位置变化的监听
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "注册地理位置变化的监听失败，检测是否授予权限");
            return;
        }
        _weakReference = new WeakReference<>(context);
        _myLocationListener = myLocationListener;
        _locationManager = (LocationManager) _weakReference.get().getSystemService(Context.LOCATION_SERVICE);
        _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, this);
    }

    /**
     * @param context
     * @param time               位置更新的最短时间
     * @param distance           位置更新的最小距离
     * @param myLocationListener
     * @throws Exception
     */
    public static void NewInstance(Activity context, long time, float distance, MyLocationListener myLocationListener) {
        _myLocationUtil = new MyLocationUtil(context, time, distance, myLocationListener);
    }

    /**
     * 开启定位
     *
     * @param isOpenGpsSetting 是否打开GPS设置界面
     * @throws Exception
     */
    public static void Start(boolean isOpenGpsSetting) {
        boolean isOpenGps = _locationManager.isProviderEnabled(LOCATETYPE);
        if (!isOpenGps && isOpenGpsSetting && Build.VERSION.SDK_INT > 15) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            _weakReference.get().startActivityForResult(intent, 2);
            return;
        }
        if (ActivityCompat.checkSelfPermission(_weakReference.get(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_weakReference.get(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "GPS打开失败，检查是否授予权限");
            return;
        }
        _myLocationListener.OnUpdateProviderStatus(1);
        //通过设备支持的定位方式来获得位置信息
        List<String> providerList = _locationManager.getProviders(true);
        //GPS定位，通过卫星获取定位信息
        String providerStr = "";
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            providerStr += LocationManager.GPS_PROVIDER.toUpperCase() + "、";
        }
        //网络定位，通过基站和wifi获取定位信息
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            providerStr += LocationManager.NETWORK_PROVIDER.toUpperCase() + "、";
        }
        //被动定位，第三方应用使用了定位系统会保存下来，通过此方式可以获取最近一次位置信息
        if (providerList.contains(LocationManager.PASSIVE_PROVIDER)) {
            providerStr += LocationManager.PASSIVE_PROVIDER.toUpperCase();
        }
        Log.i(TAG, "该设备支持的位置提供器：" + providerStr);
        _myLocationListener.OnUpdateProviders(providerStr);
        Location location = null;
        for (String temp : providerList) {
            Location tempLocation = _locationManager.getLastKnownLocation(temp);
            if (null == tempLocation) {
                continue;
            }
            //数值越低越精确
            if (location == null || tempLocation.getAccuracy() < location.getAccuracy()) {
                location = tempLocation;
            }
        }
        if (null == location) {
            Log.e(TAG, "定位失败");
            return;
        }
        Log.i(TAG, "定位成功，经度：" + location.getLongitude() + "，纬度：" + location.getLatitude());
        _myLocationListener.OnLocationChanged(location);
    }

    /**
     * 终止GPS定位
     */
    public static void Stop() {
        _myLocationUtil.RemoveUpdate();
    }

    private void RemoveUpdate() {
        _locationManager.removeUpdates(this);
    }

    /**
     * 位置改变
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "位置更新，经度：" + location.getLongitude() + "，纬度：" + location.getLatitude());
        _myLocationListener.OnLocationChanged(location);
    }

    /**
     * 定位状态改变，该方法已被弃用，并不能监听到服务状态
     *
     * @param provider
     * @param status
     * @param extras   设置参数，如高精度、低功耗等
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * 定位打开
     *
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "定位服务开启");
        _myLocationListener.OnUpdateProviderStatus(1);
    }

    /**
     * 定位关闭
     *
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "定位服务关闭");
        _myLocationListener.OnUpdateProviderStatus(0);
    }

}
