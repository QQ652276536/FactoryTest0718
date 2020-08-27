package com.zistone.factorytest0718;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Locale;

/**
 * GPS测试
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class GpsActivity extends BaseActivity implements LocationListener {

    private static final String TAG = "GpsActivity";
    private static final String LOCATETYPE = LocationManager.GPS_PROVIDER;

    private TextView _txtState, _txtProvider, _txtLat, _txtLot, _txtAddress;
    private Geocoder _geocoder;
    private GeocodeTask _geocodeTask;
    private LocationManager _locationManager;
    private MyLocationListener _myLocationListener;
    private boolean _isThreadRun = false;

    public interface MyLocationListener {
        void OnLocationChanged(Location location);

        void OnUpdateProviderStatus(int status);

        void OnUpdateProviders(String providers);
    }

    class GeocodeTask extends AsyncTask<Double, Void, String> {

        /**
         * 执行线程任务前
         */
        @Override
        protected void onPreExecute() {
        }

        /**
         * 耗时操作
         *
         * @param values
         * @return
         */
        @Override
        protected String doInBackground(Double... values) {
            List<Address> locationList;
            String addressLine = null;
            try {
                locationList = _geocoder.getFromLocation(values[0], values[1], 1);
                if (null != locationList && locationList.size() > 0) {
                    Address address = locationList.get(0);
                    //周边信息，包括街道等
                    addressLine = address.getAddressLine(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return addressLine;
        }

        /**
         * 执行完毕
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "坐标反查：" + result);
            if (null != result && !"".equals(result)) {
                _txtAddress.setText(result);
            }
        }

        /**
         * 取消
         */
        @Override
        protected void onCancelled() {
            _isThreadRun = true;
        }
    }

    /**
     * 开始定位
     *
     * @param providerList
     * @throws Exception
     */
    private void Start(List<String> providerList) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 102:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _locationManager.removeUpdates(this);
        _isThreadRun = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_gps);
        SetBaseContentView(R.layout.activity_gps);
        _txtState = findViewById(R.id.txt_state_gps);
        _txtProvider = findViewById(R.id.txt_provider_gps);
        _txtLot = findViewById(R.id.txt_lot_gps);
        _txtLat = findViewById(R.id.txt_lat_gps);
        _txtAddress = findViewById(R.id.txt_address_gps);
        _btnPass.setEnabled(false);
        _geocoder = new Geocoder(this, Locale.getDefault());
        //始化位置监听
        _myLocationListener = new MyLocationListener() {
            @Override
            public void OnLocationChanged(Location location) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _txtLat.setText(String.format("%.6f", location.getLatitude()));
                        _txtLot.setText(String.format("%.6f", location.getLongitude()));
                        _geocodeTask = new GeocodeTask();
                        _geocodeTask.execute(new Double[]{location.getLatitude(), location.getLongitude()});
                    }
                });
            }

            @Override
            public void OnUpdateProviderStatus(int status) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (status) {
                            case 0:
                                _txtState.setText("定位服务关闭");
                                _txtState.setTextColor(Color.RED);
                                _btnPass.setEnabled(false);
                                break;
                            case 1:
                                _txtState.setText("定位服务开启");
                                _txtState.setTextColor(SPRING_GREEN);
                                _btnPass.setEnabled(true);
                                break;
                        }
                    }
                });
            }

            @Override
            public void OnUpdateProviders(String providers) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _txtProvider.setText(providers);
                    }
                });
            }
        };
        _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //检查权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "GPS打开失败，检查是否授予权限");
            return;
        }
        //位置更新的最短时间为10秒，最短距离为1米
        _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10 * 1000, 1, this);
        boolean isOpenGps = _locationManager.isProviderEnabled(LOCATETYPE);
        //位置没打开，是否打开位置设置界面
        if (!isOpenGps && Build.VERSION.SDK_INT > 15) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 101);
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
        //实时定位
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!_isThreadRun) {
                    try {
                        Start(providerList);
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
