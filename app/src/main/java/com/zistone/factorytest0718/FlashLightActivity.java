package com.zistone.factorytest0718;

import android.content.ContentResolver;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zistone.factorytest0718.util.MyProgressDialogUtil;

import java.io.IOException;

public class FlashLightActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = "FlashLightActivity";

    private Camera _camera;
    private SurfaceView _surfaceView;
    private SurfaceHolder _surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_flash_light);
        SetBaseContentView(R.layout.activity_flash_light);
        _surfaceView = this.findViewById(R.id.sf_flashlight);
        _surfaceHolder = _surfaceView.getHolder();
        _surfaceHolder.addCallback(this);
        _surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        PackageManager packageManager = this.getPackageManager();
        FeatureInfo[] featureInfoArray = packageManager.getSystemAvailableFeatures();
        boolean flag = false;
        for (FeatureInfo featureInfo : featureInfoArray) {
            //判断设备是否支持闪光灯
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(featureInfo.name)) {
                flag = true;
            }
        }
        if (!flag) {
            MyProgressDialogUtil.ShowWarning(this, "知道了", "警告", "该设备不支持闪光灯，无法使用此功能！", false, () -> {
                Fail();
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (_camera != null) {
            Log.i(TAG, "关闭闪光灯");
            _camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            _camera.setParameters(_camera.getParameters());
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.i(TAG, "相机打开");
            _camera = Camera.open();
            if (_camera != null) {
                //打开闪光灯
                _camera.startPreview();
                Camera.Parameters parameter = _camera.getParameters();
                parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                _camera.setParameters(parameter);
                Log.i(TAG, "闪光灯打开");
            }
        } catch (Exception e) {
            Log.e(TAG, "相机打开异常：相机被占用，请先关闭！");
        }
    }

    /**
     * 判断是否开启了自动亮度调节
     *
     * @param
     * @return
     */
    public boolean isAutoBrightness(ContentResolver aContentResolver) {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(aContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (_camera != null) {
                _camera.setPreviewDisplay(holder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}