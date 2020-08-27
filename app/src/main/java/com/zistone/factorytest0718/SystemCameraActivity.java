package com.zistone.factorytest0718;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;

/**
 * 调用系统相机
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class SystemCameraActivity extends BaseActivity {

    private static final String TAG = "SystemCameraActivity";
    private static final int CHANGE_TO_BACK_CAMERA = 1;
    //前后置摄像头
    private int FRONT = 2, BACK = 1;
    private boolean _already = false;
    private Handler _handler = new MainHandler();

    private class MainHandler extends Handler {
        public MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_TO_BACK_CAMERA: {
                    OpenBackCamera();
                    break;
                }
            }
        }
    }

    private void OpenBackCamera() {
        ComponentName componentName = new ComponentName("com.simplemobiletools.camera", "com.simplemobiletools.camera.activities.MainActivity\n");
        Intent intent_camera = new Intent();
        intent_camera.setComponent(componentName);
        startActivityForResult(intent_camera, FRONT);
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
        _already = true;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (!_already) {
                _already = true;
                _handler.sendEmptyMessageDelayed(CHANGE_TO_BACK_CAMERA, 100);
            } else {
                Pass();
            }
        } else {
            Fail();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
