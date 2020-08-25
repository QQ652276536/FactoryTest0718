package com.zistone.factorytest0718;

import android.app.Activity;
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
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

public class FlashLightActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = "FlashLightActivity";

    //进入APP时的背光亮度值
    private int _lightValue;
    //进入APP时，是否为自动调节亮度状态
    private boolean _autoBrightness = false;
    private Camera _camera;
    private SurfaceView _surfaceView;
    private SurfaceHolder _surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_flash_light);
        SetBaseContentView(R.layout.activity_flash_light);

        _surfaceView = (SurfaceView) this.findViewById(R.id.sf_flashlight);
        _surfaceHolder = _surfaceView.getHolder();
        _surfaceHolder.addCallback(this);
        _surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        if (isAutoBrightness(getContentResolver())) {
            _autoBrightness = true;
        }

        _lightValue = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);

        PackageManager pm = this.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo f : features) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name))   //判断设备是否支持闪光灯
            {
                Log.d(TAG, "支持闪光灯");
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Closeshoudian();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Openshoudian();
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

    /**
     * 停止自动亮度调节
     *
     * @param activity
     */
    public void stopAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    /**
     * 恢复自动亮度调节
     *
     * @param activity
     */
    public void setAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * 打开手电筒
     */
    public void Openshoudian() {
        //异常处理一定要加，否则Camera打开失败的话程序会崩溃
        try {
            Log.d(TAG, "_camera打开");
            _camera = Camera.open();
        } catch (Exception e) {
            Log.d(TAG, "Camera打开有问题");
            Toast.makeText(this, "Camera被占用，请先关闭", Toast.LENGTH_SHORT).show();
        }

        if (_camera != null) {
            //打开闪光灯
            _camera.startPreview();
            Camera.Parameters parameter = _camera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            _camera.setParameters(parameter);
            Log.d(TAG, "闪光灯打开");

            //先关闭自动调节背光功能，才可以调节背光
            if (_autoBrightness) {
                stopAutoBrightness(this);
            }

            //将背光设置为最亮
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = Float.valueOf(255) * (1f / 255f);
            getWindow().setAttributes(lp);
        }
    }

    /**
     * 关闭手电筒
     */
    public void Closeshoudian() {
        if (_camera != null) {
            //关闭闪光灯
            Log.d(TAG, "closeCamera()");
            _camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            _camera.setParameters(_camera.getParameters());
            _camera.stopPreview();
            _camera.release();
            _camera = null;
            //恢复进入程序前的背光值
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = Float.valueOf(_lightValue) * (1f / 255f);
            getWindow().setAttributes(lp);
            //如果进入APP时背光为自动调节，则退出时需要恢复为自动调节状态
            if (_autoBrightness) {
                setAutoBrightness(this);
            }
        }
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