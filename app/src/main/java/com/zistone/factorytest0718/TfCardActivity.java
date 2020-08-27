package com.zistone.factorytest0718;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyProgressDialogUtil;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * TF卡信息
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class TfCardActivity extends BaseActivity {

    private static final String TAG = "TfCardActivity";

    private TextView _txt;

    final BroadcastReceiver _broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //SD/TF卡插入
            if (Objects.equals(intent.getAction(), Intent.ACTION_MEDIA_MOUNTED)) {
                Log.i(TAG, "检测到SD/TF卡插入");
                IsExistCard();
            }
            //SD/TF卡拨出
            if (Objects.equals(intent.getAction(), Intent.ACTION_MEDIA_UNMOUNTED)) {
                if (_btnPass.isEnabled()) {
                    Pass();
                } else {
                    Log.i(TAG, "检测到SD/TF卡拨出");
                    _txt.setTextColor(Color.RED);
                    _txt.setText("SD/TF卡已拨出");
                    _btnPass.setEnabled(false);
                }
            }
        }
    };

    /**
     * 判断外置SD/TF卡是否挂载
     *
     * @return
     */
    public boolean IsExistCard() {
        boolean result = false;
        StorageManager mStorageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Method getState = storageVolumeClazz.getMethod("getState");
            Object obj = null;
            try {
                obj = getVolumeList.invoke(mStorageManager);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            final int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(obj, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                String state = (String) getState.invoke(storageVolumeElement);
                if (removable && state.equals(Environment.MEDIA_MOUNTED)) {
                    result = true;
                    Log.i(TAG, "SD/TF卡的路径：" + path);
                    StatFs statFs = new StatFs(path);
                    long size = statFs.getBlockSizeLong();
                    long count = statFs.getBlockCountLong();
                    double total = size * count / 1000.00 / 1000.00 / 1000.00;
                    Log.i(TAG, "全部存储空间：" + String.format("%.2f", total) + "GB");
                    long availableSize = statFs.getAvailableBlocksLong();
                    double canUse = size * availableSize / 1000.00 / 1000.00 / 1000.00;
                    Log.i(TAG, "可用存储空间：" + String.format("%.2f", canUse) + "GB");
                    _btnPass.setEnabled(true);
                    _txt.setTextColor(SPRING_GREEN);
                    _txt.setText("检测到SD/TF卡\n共" + String.format("%.2f", total) + "GB");
                    _btnPass.setEnabled(true);
                    MyProgressDialogUtil.ShowCountDownTimerWarning(this, "知道了", 3 * 1000, "提示", "TF卡测试已通过！\n\n卡存储空间：" + String.format("%.2f", total) + "GB", false, () -> {
                        MyProgressDialogUtil.DismissAlertDialog();
                        Pass();
                    });
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(_broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_tf_card);
        SetBaseContentView(R.layout.activity_tf_card);
        _txt = findViewById(R.id.txt_tfcard);
        _btnPass.setEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(_broadcastReceiver, filter);
        IsExistCard();
    }

}

