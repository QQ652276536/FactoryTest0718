package com.zistone.factorytest0718.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class PowerKeyObserver {

    private static final String TAG = "PowerKeyObserver";

    private Context mContext;
    private IntentFilter mIntentFilter;
    private OnPowerKeyListener mOnPowerKeyListener;
    private PowerKeyBroadcastReceiver mPowerKeyBroadcastReceiver;

    public PowerKeyObserver(Context context) {
        this.mContext = context;
    }

    //注册广播接收者
    public void startListen() {
        mIntentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mPowerKeyBroadcastReceiver = new PowerKeyBroadcastReceiver();
        mContext.registerReceiver(mPowerKeyBroadcastReceiver, mIntentFilter);
        Log.i(TAG,"开始监听");
    }

    //取消广播接收者
    public void stopListen() {
        if (mPowerKeyBroadcastReceiver != null) {
            mContext.unregisterReceiver(mPowerKeyBroadcastReceiver);
            Log.i(TAG,"停止监听");
        }
    }

    // 对外暴露接口
    public void setHomeKeyListener(OnPowerKeyListener powerKeyListener) {
        mOnPowerKeyListener = powerKeyListener;
    }

    // 回调接口
    public interface OnPowerKeyListener {
        public void onPowerKeyPressed();
    }

    //广播接收者
    class PowerKeyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                mOnPowerKeyListener.onPowerKeyPressed();
            }
        }
    }

}
