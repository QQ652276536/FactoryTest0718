package com.zistone.factorytest0718;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class WifiActivity extends BaseActivity {

    private static final String TAG = "WifiActivity";

    private boolean _isPass = false;

    /**
     * Wifi搜索的监听
     */
    public interface DeviceSearchListener {
        /**
         * 开启搜索
         */
        void onDiscoveryStart();

        /**
         * 搜索完成
         */
        void onDiscoveryFinsh();

        /**
         * 搜索到Wifi
         */
        void onDeviceFounded();
    }

    /**
     * 通过广播接收器来获取Wifi
     */
    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Wifi开关状态
            if (TextUtils.equals(action, WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.i(TAG, "Wifi正在关闭");
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.i(TAG, "Wifi已关闭");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.i(TAG, "Wifi正在打开");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.i(TAG, "Wifi已打开");
                        _deviceSearchListener.onDiscoveryStart();
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        Log.i(TAG, "Wifi状态未知");
                        break;
                }
            }
            //Wifi连接状态
            else if (TextUtils.equals(action, WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            }
            //Wifi列表变化
            else if (TextUtils.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                _scanResultList = _wifiManager.getScanResults();
                for (ScanResult scanResult : _scanResultList)
                    Log.i(TAG, "扫描到Wifi，名称：" + scanResult.SSID + "，地址：" + scanResult.BSSID + "，信号强度：" + scanResult.level + "，加密方式：" + scanResult.capabilities);
                _deviceSearchListener.onDeviceFounded();
            }
        }
    }

    private BaseAdapter _baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return _scanResultList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView)
                convertView = _layoutInflater.inflate(R.layout.item_wifi, null);
            ImageView imageView = convertView.findViewById(R.id.iv_wifi);
            TextView txtName = convertView.findViewById(R.id.tv_ssid);
            TextView txtType = convertView.findViewById(R.id.tv_type);
            TextView txtAddress = convertView.findViewById(R.id.tv_bssid);
            TextView txtRssi = convertView.findViewById(R.id.tv_level);
            ScanResult wifiInfo = _scanResultList.get(position);
            String ssid = wifiInfo.SSID;
            String capabilities = wifiInfo.capabilities;
            String bssid = wifiInfo.BSSID;
            int rssi = wifiInfo.level;
            imageView.setImageDrawable(getDrawable(R.drawable.wifi_icon));
            txtName.setText(ssid);
            txtType.setText("");
            if (!TextUtils.isEmpty(capabilities)) {
                if (capabilities.toUpperCase().contains("WPA") || capabilities.toUpperCase().contains("WEP")) {
                    if (rssi <= -70) {
                        imageView.setImageDrawable(getDrawable(R.drawable.wifi_icon_lock1));
                    } else if (rssi > -70 && rssi <= -80) {
                        imageView.setImageDrawable(getDrawable(R.drawable.wifi_icon_lock2));
                    } else {
                        imageView.setImageDrawable(getDrawable(R.drawable.wifi_icon_lock3));
                    }
                    if (capabilities.toUpperCase().contains("WPA")) {
                        txtType.setText("加密（WPA）");
                    } else if (capabilities.toUpperCase().contains("WEP")) {
                        txtType.setText("加密（WEP）");
                    }
                    if (rssi >= -75 && !_isPass) {
                        _isPass = true;
                        _btnPass.setEnabled(true);
                        MyProgressDialogUtil.ShowWarning(WifiActivity.this, "提示",
                                "Wifi测试已通过！\n\nWifi名称：" + ssid + "\nWifi地址：" + bssid + "\n信号强度：" + rssi, false,
                                () -> Pass());
                    }
                }
            }
            txtAddress.setText(bssid);
            txtRssi.setText(rssi + "dBm");
            return convertView;
        }
    };
    private LayoutInflater _layoutInflater;
    private List<ScanResult> _scanResultList = new ArrayList<>();
    private ListView _listView;
    private WifiBroadcastReceiver _wifiBroadcastReceiver;
    private GifImageView _refreshGif;
    private MaterialRefreshLayout _materialRefreshLayout;
    private DeviceSearchListener _deviceSearchListener;
    private MaterialRefreshListener _materialRefreshListener;
    private TextView _txt;
    private WifiManager _wifiManager;

    private void InitWifi() {
        //注册Wifi广播
        _wifiBroadcastReceiver = new WifiBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //Wifi开关状态
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //Wifi连接状态
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //Wifi列表
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(_wifiBroadcastReceiver, intentFilter);
        _wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (_wifiManager.isWifiEnabled()) {
            //如果本地Wifi已经打开则直接开始搜索Wifi，否则监听本地Wifi的状态，等它打开以后才开始搜索
            _wifiManager.startScan();
            Toast.makeText(this, "本地Wifi已开启", Toast.LENGTH_SHORT).show();
        } else {
            _wifiManager.setWifiEnabled(true);
        }
    }

    private void InitListener() {
        //搜索Wifi的
        _deviceSearchListener = new DeviceSearchListener() {
            @Override
            public void onDiscoveryStart() {
                _txt.setVisibility(View.GONE);
                _refreshGif.setVisibility(View.VISIBLE);
                _wifiManager.startScan();
            }

            @Override
            public void onDiscoveryFinsh() {
                _txt.setVisibility(View.VISIBLE);
                _refreshGif.setVisibility(View.GONE);
            }

            @Override
            public void onDeviceFounded() {
                _baseAdapter.notifyDataSetChanged();
            }
        };
        //下拉刷新的
        _materialRefreshListener = new MaterialRefreshListener() {
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
                materialRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        _scanResultList.clear();
                        //使用notifyDataSetChanged()会保存当前的状态信息，然后更新适配器里的内容
                        //                        _baseAdapter.notifyDataSetChanged();
                        //使用setAdapter()不会保存当前的状态信息，会使页面回到顶部，不会停留在之前的位置
                        _listView.setAdapter(_baseAdapter);
                        _wifiManager.startScan();
                        //结束下拉刷新
                        materialRefreshLayout.finishRefreshing();
                    }
                }, 500);
            }

            @Override
            public void onfinish() {
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(_wifiBroadcastReceiver);
        _scanResultList.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_wifi);
        SetBaseContentView(R.layout.activity_wifi);
        _layoutInflater = LayoutInflater.from(this);
        _listView = findViewById(R.id.lv_wifi);
        _listView.setAdapter(_baseAdapter);
        _refreshGif = findViewById(R.id.loading_wifi);
        _materialRefreshLayout = findViewById(R.id.refresh_wifi);
        _txt = findViewById(R.id.txt_wifi);
        _txt.setVisibility(View.GONE);
        InitWifi();
        InitListener();
        _materialRefreshLayout.setMaterialRefreshListener(_materialRefreshListener);
        //        _materialRefreshLayout.autoRefresh();
        _btnPass.setEnabled(false);
    }
}
