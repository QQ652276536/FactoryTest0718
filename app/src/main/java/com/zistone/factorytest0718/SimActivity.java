package com.zistone.factorytest0718;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.zistone.factorytest0718.R;

public class SimActivity extends AppCompatActivity {
    private static final String TAG = "SimActivity";

    private TextView _txtSim, _txtCountryCode, _txtUserId, _txtDeviceId, _txtPhone, _txtLocation, _txtMcc, _txtNetWorkCode, _txtServiceProvider, _txtSequence, _txtDataState;
    private TelephonyManager _telephonyManager;

    /**
     * 获取SIM卡信息
     * <p>
     * 解释：
     * IMSI是国际移动用户识别码的简称(International Mobile Subscriber Identity)
     * IMSI共有15位，其结构如下：
     * MCC+MNC+MIN
     * MCC：Mobile Country Code，移动国家码，共3位，中国为460
     * MNC:Mobile NetworkCode，移动网络码，共2位
     * 在中国，移动的代码为电00和02，联通的代码为01，电信的代码为03
     * 合起来就是（也是Android手机中APN配置文件中的代码）：
     * 中国移动：46000 46002
     * 中国联通：46001
     * 中国电信：46003
     * 比如一个典型的IMSI号就是460030912121001
     * <p>
     * IMEI是International Mobile Equipment Identity （国际移动设备标识）的简称
     * IMEI由15位数字组成的”电子串号”，它与每台手机一一对应，而且该码是全世界唯一的
     * 其组成为：
     * 1. 前6位数(TAC)是”型号核准号码”，一般代表机型
     * 2. 接着的2位数(FAC)是”最后装配号”，一般代表产地
     * 3. 之后的6位数(SNR)是”串号”，一般代表生产顺序号
     * 4. 最后1位数(SP)通常是”0″，为检验码，目前暂备用
     */
    private void GetSimInfoAndSetTextView() {
        _telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //SIM卡状态
        // SIM_STATE_UNKNOWN 未知状态 0
        // SIM_STATE_ABSENT 没插卡 1
        // SIM_STATE_PIN_REQUIRED 锁定状态，需要用户的PIN码解锁 2
        // SIM_STATE_PUK_REQUIRED 锁定状态，需要用户的PUK码解锁 3
        // SIM_STATE_NETWORK_LOCKED 锁定状态，需要网络的PIN码解锁 4
        // SIM_STATE_READY 就绪状态 5
        int simState = _telephonyManager.getSimState();
        if (simState == _telephonyManager.SIM_STATE_READY) {
            Log.i(TAG, "检测到SIM卡");
            _txtSim.setText("检测到SIM卡");
            _txtSim.setTextColor(Color.parseColor("#3CB371"));
        } else if (simState == _telephonyManager.SIM_STATE_ABSENT) {
            Log.i(TAG, "无SIM卡");
            _txtSim.setText("无SIM卡");
            _txtSim.setTextColor(Color.RED);
        } else {
            Log.i(TAG, "SIM卡被锁定或状态未知");
            _txtSim.setText("SIM卡被锁定或状态未知");
            _txtSim.setTextColor(Color.RED);
        }
        //电话方位
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            _txtSim.setText("功能无权限");
            _txtSim.setTextColor(Color.RED);
        } else {
            //SIM卡的国家码
            String countryCode = _telephonyManager.getSimCountryIso();
            _txtCountryCode.setText(countryCode);
            //唯一的用户ID，例如：IMSI（国际移动用户识别码）
            String userId = _telephonyManager.getSubscriberId();
            _txtUserId.setText(userId);
            //唯一的设备ID，如果是GSM网络，返回IMEI；如果是CDMA网络，返回MEID
            String deviceId = _telephonyManager.getDeviceId();
            _txtDeviceId.setText(deviceId);
            //手机号
            String phone = _telephonyManager.getLine1Number();
            if (!TextUtils.isEmpty(phone))
                _txtPhone.setText(phone);
            //手机方位
            CellLocation location = _telephonyManager.getCellLocation();
            _txtLocation.setText(location + "");
            //MCC+MNC(mobile country code + mobile network code)
            String mcc = _telephonyManager.getNetworkOperator();
            _txtMcc.setText(mcc);
            //网络码，获取SIM卡提供的移动国家码和移动网络码，5或6位的十进制数字
            String netWorkCode = _telephonyManager.getSimOperator();
            _txtNetWorkCode.setText(netWorkCode);
            //服务商名称
            String serviceProvder = _telephonyManager.getSimOperatorName();
            _txtServiceProvider.setText(serviceProvder);
            //SIM卡序列
            String simSequence = _telephonyManager.getSimSerialNumber();
            _txtSequence.setText(simSequence);
            //数据连接状态
            // DATA_UNKNOWN 数据连接状态：未知
            // DATA_CONNECTED 数据连接状态：已连接
            // DATA_CONNECTING 数据连接状态：正在连接
            // DATA_DISCONNECTED 数据连接状态：断开
            // DATA_SUSPENDED 数据连接状态：暂停
            int dataState = _telephonyManager.getDataState();
            if (dataState == _telephonyManager.DATA_UNKNOWN) {
                _txtDataState.setText("状态未知");
            } else if (dataState == _telephonyManager.DATA_DISCONNECTED) {
                _txtDataState.setText("已关闭");
            } else if (dataState == _telephonyManager.DATA_CONNECTING) {
                _txtDataState.setText("正在打开");
            } else if (dataState == _telephonyManager.DATA_CONNECTED) {
                _txtDataState.setText("已打开");
            } else if (dataState == _telephonyManager.DATA_SUSPENDED) {
                _txtDataState.setText("无网络");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim);
        _txtSim = findViewById(R.id.txt_state_sim);
        _txtCountryCode = findViewById(R.id.txt_countrycode_sim);
        _txtUserId = findViewById(R.id.txt_userid_sim);
        _txtDeviceId = findViewById(R.id.txt_deviceid_sim);
        _txtPhone = findViewById(R.id.txt_phone_sim);
        _txtLocation = findViewById(R.id.txt_location_sim);
        _txtMcc = findViewById(R.id.txt_mcc_sim);
        _txtNetWorkCode = findViewById(R.id.txt_networkcode_sim);
        _txtServiceProvider = findViewById(R.id.txt_serviceprovider_sim);
        _txtSequence = findViewById(R.id.txt_sequence_sim);
        _txtDataState = findViewById(R.id.txt_data_sim);
        GetSimInfoAndSetTextView();
    }

}