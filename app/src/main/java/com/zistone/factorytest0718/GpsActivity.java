package com.zistone.factorytest0718;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyLocationUtil;

import java.util.List;
import java.util.Locale;

public class GpsActivity extends BaseActivity {

    private static final String TAG = "GpsActivity";

    private TextView _txtState, _txtProvider, _txtLat, _txtLot, _txtAddress;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLocationUtil.Stop();
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
        //位置更新的最短时间为10秒，最短距离为1米
        MyLocationUtil.NewInstance(this, 10 * 1000, 1, new MyLocationUtil.MyLocationListener() {
            @Override
            public void OnLocationChanged(Location location) {
                _txtLat.setText(location.getLatitude() + "");
                _txtLot.setText(location.getLongitude() + "");
                Geocoder geocoder = new Geocoder(GpsActivity.this, Locale.getDefault());
                List<Address> locationList;
                try {
                    locationList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (null != locationList && locationList.size() > 0) {
                        Address address = locationList.get(0);
                        //周边信息，包括街道等
                        String addressLine = address.getAddressLine(0);
                        Log.i(TAG, "坐标反查：" + addressLine);
                        _txtAddress.setText(addressLine);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnUpdateProviderStatus(int status) {
                switch (status) {
                    case 0:
                        _txtState.setText("定位服务关闭");
                        _txtState.setTextColor(Color.RED);
                        break;
                    case 1:
                        _txtState.setText("定位服务开启");
                        _txtState.setTextColor(Color.parseColor("#3CB371"));
                        break;
                }
            }

            @Override
            public void OnUpdateProviders(String providers) {
                _txtProvider.setText(providers);
            }
        });
        MyLocationUtil.Start(false);
    }

}
