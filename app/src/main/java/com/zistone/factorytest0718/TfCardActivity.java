package com.zistone.factorytest0718;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class TfCardActivity extends BaseActivity {

    private static final String TAG = "TFCardTest";

    private TextView _txt;

    final BroadcastReceiver _broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //SD/TF卡已插入
            if (Objects.equals(intent.getAction(), Intent.ACTION_MEDIA_MOUNTED)) {
                _txt.setTextColor(Color.GREEN);
                _txt.setText("已检测到SD/TF卡");
                _btnPass.setEnabled(true);
            }
            //SD/TF卡已拨出
            if (Objects.equals(intent.getAction(), Intent.ACTION_MEDIA_UNMOUNTED)) {
                if (_btnPass.isEnabled()) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(ARG_PARAM1, PASS);
                    TfCardActivity.this.setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    _txt.setTextColor(Color.RED);
                    _txt.setText("SD/TF卡已拨出");
                    _btnPass.setEnabled(false);
                }
            }
        }
    };

    /**
     * 判断外置sd卡是否挂载
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
        _btnPass.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, PASS);
            setResult(RESULT_OK, intent);
            finish();
        });
        _btnFail.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, FAIL);
            setResult(RESULT_OK, intent);
            finish();
        });
        if (IsExistCard()) {
            _btnPass.setEnabled(true);
            _txt.setTextColor(Color.GREEN);
            _txt.setText("已检测到SD/TF卡");
        } else {
            _btnPass.setEnabled(false);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(_broadcastReceiver, filter);
    }

}

