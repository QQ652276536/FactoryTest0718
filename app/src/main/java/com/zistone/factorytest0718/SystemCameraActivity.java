package com.zistone.factorytest0718;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 调用系统相机
 * 先测试前置摄像头，通过后紧接着测试后置摄像头，也通过后才算功能正常
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class SystemCameraActivity extends BaseActivity {

    private static final String TAG = "SystemCameraActivity";

    private int _currentDirection = 1;

    /**
     * 这种打开相机的方式在WD220的设备上有异常，先保留，看看其它设备是否可用
     *
     * @param dirction
     */
    private void OpenCamera(int dirction) {
        ComponentName componentName = new ComponentName("com.simplemobiletools.camera", "com.simplemobiletools.camera.activities.MainActivity\n");
        Intent intent_camera = new Intent();
        intent_camera.setComponent(componentName);
        startActivityForResult(intent_camera, dirction);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("android.intent.extras.CAMERA_FACING", _currentDirection);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode=" + requestCode + "，resultCode=" + resultCode);
        try {
            switch (requestCode) {
                //先测试前置摄像头，通过后测试后置摄像头，通过后才算该功能正常
                case 101:
                    if (resultCode == RESULT_OK) {
                        if (_currentDirection == 1) {
                            _currentDirection = 2;
                            //打开后置摄像头
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra("android.intent.extras.CAMERA_FACING", _currentDirection);
                            startActivityForResult(intent, 101);
                        } else if (_currentDirection == 2) {
                            Pass();
                        }
                    } else {
                        Fail();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Fail();
        }
    }

}
