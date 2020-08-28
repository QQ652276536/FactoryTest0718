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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 闪光灯测试
 * TODO 信号灯的测试还没有实现，暂时先不用管
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class FlashLightActivity extends BaseActivity implements SurfaceHolder.Callback {

    private static final String TAG = "FlashLightActivity";
    private static final String GREEN = "/sys/class/leds/green/brightness";
    private static final String RED = "/sys/class/leds/red/brightness";
    private static byte[] OPEN = "0".getBytes();
    private static byte[] CLOSE = "1".getBytes();

    private Camera _camera;
    private SurfaceView _surfaceView;
    private SurfaceHolder _surfaceHolder;
    private FileOutputStream _fileOutputStream;
    private Timer _timer;
    private TimerTask _timerTask;

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
    protected void onStop() {
        super.onStop();
        if (_camera != null) {
            Log.i(TAG, "关闭闪光灯");
            _camera.getParameters().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            _camera.setParameters(_camera.getParameters());
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
        if (null != _timerTask)
            _timerTask.cancel();
        if (null != _timer)
            _timer.cancel();
        try {
            _fileOutputStream = new FileOutputStream(RED);
            _fileOutputStream.write(CLOSE);
            _fileOutputStream.close();
            _fileOutputStream = new FileOutputStream(GREEN);
            _fileOutputStream.write(CLOSE);
            _fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "关闭信号灯");
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
            _timer = new Timer();
            _timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        //重置
                        _fileOutputStream = new FileOutputStream(GREEN);
                        _fileOutputStream.write(CLOSE);
                        _fileOutputStream.close();
                        _fileOutputStream = new FileOutputStream(RED);
                        _fileOutputStream.write(CLOSE);
                        _fileOutputStream.close();
                        //绿灯
                        _fileOutputStream = new FileOutputStream(GREEN);
                        _fileOutputStream.write(OPEN);
                        Thread.sleep(500);
                        _fileOutputStream.write(CLOSE);
                        _fileOutputStream.close();
                        //红灯
                        _fileOutputStream = new FileOutputStream(RED);
                        _fileOutputStream.write(OPEN);
                        Thread.sleep(500);
                        _fileOutputStream.write(CLOSE);
                        _fileOutputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            _timer.schedule(_timerTask, 0, 1 * 1000);
            Log.i(TAG, "信号灯打开");
        } catch (Exception e) {
            Log.e(TAG, "相机打开异常：相机被占用，请先关闭！");
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