package com.zistone.factorytest0718;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SystemCameraActivity extends AppCompatActivity {
    private static final String TAG = "SystemCameraActivity";
    private static final int CHANGE_TO_BACK_CAMERA = 1;

    private int BACK = 1, FRONT = 2;
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _already = true;
        Intent mIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startActivityForResult(mIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BACK) {
            Log.i(TAG, "requestCode == BACK");
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "resultCode == RESULT_OK");
                if (!_already) {
                    _already = true;
                    _handler.sendEmptyMessageDelayed(CHANGE_TO_BACK_CAMERA, 30);
                } else {
                    finish();
                }
            } else {
                finish();
            }

        } else if (requestCode == FRONT) {
            if (resultCode == RESULT_OK) {
                finish();
            } else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
