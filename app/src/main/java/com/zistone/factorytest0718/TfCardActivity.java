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
                }
            }
        }
    };

    /**
     * 判断外置sd卡是否挂载
     *
     * @return
     */
    public boolean isExistCard2() {
        boolean result = false;
        StorageManager mStorageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
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
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 判断外置sd卡是否挂载
     *
     * @return
     */
    private boolean isExistCard() {
        boolean result = false;
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        try {
            Method method1 = StorageManager.class.getMethod("getVolumeList");
            method1.setAccessible(true);
            Object[] arrays = (Object[]) method1.invoke(storageManager);
            if (arrays != null) {
                for (Object temp : arrays) {
                    Method mRemoveable = temp.getClass().getMethod("isRemovable");
                    Boolean isRemovable = (Boolean) mRemoveable.invoke(temp);
                    try {
                        if (isRemovable) {
                            Method getPath = temp.getClass().getMethod("getPath");
                            String path = (String) mRemoveable.invoke(temp);
                            Method getState = storageManager.getClass().getMethod("getVolumeState", String.class);
                            String state = (String) getState.invoke(storageManager, path);
                            if (state.equals(Environment.MEDIA_MOUNTED)) {
                                result = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        if (isExistCard2()) {
            _btnPass.setEnabled(true);
            _txt.setTextColor(Color.GREEN);
            _txt.setText("已检测到SD/TF卡");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        registerReceiver(_broadcastReceiver, filter);
    }

}

