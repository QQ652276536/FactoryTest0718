package com.zistone.factorytest0718;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import pl.droidsonroids.gif.GifImageView;

public class BluetoothActivity extends BaseActivity {

    private static final String TAG = "BluetoothActivity";

    private boolean _isPass = false;

    /**
     * 蓝牙搜索的监听
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
         * 搜索到蓝牙
         */
        void onDeviceFounded(BluetoothDevice bluetoothDevice, int rssi);
    }

    /**
     * 搜索到的蓝牙
     */
    private class MyBluetoothDevice {
        public String _name;
        public String _address;
        public int _rssi;

        public MyBluetoothDevice(String _name, String _address, int _rssi) {
            this._name = _name;
            this._address = _address;
            this._rssi = _rssi;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof MyBluetoothDevice))
                return false;
            MyBluetoothDevice that = (MyBluetoothDevice) o;
            return _address.equals(that._address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(_address);
        }
    }

    /**
     * 定义蓝牙广播接收器来获取蓝牙
     */
    private class BluetoothBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            switch (state) {
                //本地蓝牙正在打开中
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.i(TAG, "本地蓝牙正在打开中...");
                    break;
                //本地蓝牙已经打开
                case BluetoothAdapter.STATE_ON:
                    Log.i(TAG, "本地蓝牙已经打开，开始搜索...");
                    _deviceSearchListener.onDiscoveryStart();
                    break;
                //本地蓝牙正在关闭中
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.i(TAG, "本地蓝牙正在关闭中...");
                    break;
                //本地蓝牙已经关闭
                case BluetoothAdapter.STATE_OFF:
                    Log.i(TAG, "本地蓝牙已经关闭");
                    break;
            }
            //开启搜索
            if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.i(TAG, "正在搜索附近蓝牙...");
            }
            //搜索完成
            else if (TextUtils.equals(action, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.i(TAG, "本次蓝牙搜索完毕");
                _deviceSearchListener.onDiscoveryFinsh();
            }
            //搜索到蓝牙
            else if (TextUtils.equals(action, BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                //信号强度
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.i(TAG, "搜索到蓝牙，名称：" + name + "，地址：" + address + "，信号强度：" + rssi);
                _deviceSearchListener.onDeviceFounded(device, rssi);
                if (rssi >= -75 && !_isPass) {
                    _isPass = true;
                    _btnPass.setEnabled(true);
                    MyProgressDialogUtil.ShowWarning(BluetoothActivity.this, "提示",
                            "蓝牙测试已通过！\n\n蓝牙名称：" + name + "\n蓝牙地址：" + address + "\n信号强度：" + rssi, false, () -> Pass());
                }
            }
        }
    }

    private BaseAdapter _baseAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return _myBluetoothDeviceList.size();
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
                convertView = _layoutInflater.inflate(R.layout.item_bluetooth, null);
            TextView txtName = convertView.findViewById(R.id.tv_name);
            TextView txtAddress = convertView.findViewById(R.id.tv_address);
            TextView txtRssi = convertView.findViewById(R.id.tv_rssi);
            MyBluetoothDevice device = _myBluetoothDeviceList.get(position);
            String name = device._name;
            String address = device._address;
            int rssi = device._rssi;
            txtName.setText(name);
            txtAddress.setText(address);
            txtRssi.setText(rssi + "dBm");
            return convertView;
        }
    };
    private LayoutInflater _layoutInflater;
    private List<MyBluetoothDevice> _myBluetoothDeviceList = new ArrayList<>();
    private ListView _listView;
    private BluetoothAdapter _bluetoothAdapter;
    private BluetoothBroadcastReceiver _bluetoothBroadcastReceiver;
    private GifImageView _refreshGif;
    private MaterialRefreshLayout _materialRefreshLayout;
    private DeviceSearchListener _deviceSearchListener;
    private MaterialRefreshListener _materialRefreshListener;
    private TextView _txt;

    private void InitBluetooth() {
        //获取本地蓝牙适配器
        _bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == _bluetoothAdapter) {
            MyProgressDialogUtil.ShowConfirm(this, "错误", "当前设备不支持蓝牙", false, null);
            return;
        }
        //注册蓝牙广播
        _bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        //开始搜索蓝牙
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //蓝牙搜索完毕
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //搜索到蓝牙
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(_bluetoothBroadcastReceiver, intentFilter);
        //判断蓝牙是否已开启
        if (_bluetoothAdapter.isEnabled()) {
            //如果本地蓝牙已经打开则直接开始搜索蓝牙，否则监听本地蓝牙的状态，等它打开以后才开始搜索
            _bluetoothAdapter.startDiscovery();
            Toast.makeText(this, "本地蓝牙已开启", Toast.LENGTH_SHORT).show();
        } else {
            //蓝牙没有打开，去打开蓝牙。推荐使用第二种打开蓝牙方式：
            //第一种方式：直接打开手机蓝牙，没有任何提示，需要BLUETOOTH_ADMIN权限
            //                _bluetoothAdapter.enable();
            //第二种方式：友好提示用户打开蓝牙
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
    }

    private void InitListener() {
        //搜索蓝牙的
        _deviceSearchListener = new DeviceSearchListener() {
            @Override
            public void onDiscoveryStart() {
                _txt.setVisibility(View.GONE);
                _refreshGif.setVisibility(View.VISIBLE);
                _bluetoothAdapter.startDiscovery();
            }

            @Override
            public void onDiscoveryFinsh() {
                _txt.setVisibility(View.VISIBLE);
                _refreshGif.setVisibility(View.GONE);
            }

            @Override
            public void onDeviceFounded(BluetoothDevice bluetoothDevice, int rssi) {
                MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress(), rssi);
                if (!_myBluetoothDeviceList.contains(myBluetoothDevice))
                    _myBluetoothDeviceList.add(myBluetoothDevice);
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
                        _myBluetoothDeviceList.clear();
                        //使用notifyDataSetChanged()会保存当前的状态信息，然后更新适配器里的内容
                        //                        _baseAdapter.notifyDataSetChanged();
                        //使用setAdapter()不会保存当前的状态信息，会使页面回到顶部，不会停留在之前的位置
                        _listView.setAdapter(_baseAdapter);
                        //当前正在搜索蓝牙
                        if (_bluetoothAdapter.isDiscovering())
                            _bluetoothAdapter.cancelDiscovery();
                        _bluetoothAdapter.startDiscovery();
                        //结束下拉刷新
                        materialRefreshLayout.finishRefresh();
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
        unregisterReceiver(_bluetoothBroadcastReceiver);
        _myBluetoothDeviceList.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_bluetooth);
        //将该类的布局添加到基类
        SetBaseContentView(R.layout.activity_bluetooth);
        _layoutInflater = LayoutInflater.from(this);
        _listView = findViewById(R.id.lv_bluetooth);
        _listView.setAdapter(_baseAdapter);
        _refreshGif = findViewById(R.id.loading_bluetooth);
        _materialRefreshLayout = findViewById(R.id.refresh_bluetooth);
        _txt = findViewById(R.id.txt_bluetooth);
        _txt.setVisibility(View.GONE);
        InitBluetooth();
        InitListener();
        _materialRefreshLayout.setMaterialRefreshListener(_materialRefreshListener);
        //        _materialRefreshLayout.autoRefresh();
        _btnPass.setEnabled(false);
    }
}
