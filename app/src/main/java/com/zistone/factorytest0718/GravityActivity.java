package com.zistone.factorytest0718;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.zistone.factorytest0718.util.MySensorUtil;

import static android.hardware.SensorManager.STANDARD_GRAVITY;

/**
 * 重力传感器测试
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class GravityActivity extends BaseActivity {

    private static final String TAG = "GravityActivity";

    private MySensorUtil _mySensorUtil;
    private LinearLayout _llLeft, _llTop, _llRight, _llBottom;

    private void JudgePass() {
        if (_llLeft.getVisibility() == View.VISIBLE && _llTop.getVisibility() == View.VISIBLE && _llRight.getVisibility() == View.VISIBLE && _llBottom.getVisibility() == View.VISIBLE) {
            _btnPass.setEnabled(true);
            Pass();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _mySensorUtil.UnRegister();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //                setContentView(R.layout.activity_gravity);
        SetBaseContentView(R.layout.activity_gravity);
        _btnPass.setEnabled(false);
        _mySensorUtil = MySensorUtil.GetInstance();
        _llLeft = findViewById(R.id.ll_left_gravity);
        _llTop = findViewById(R.id.ll_top_gravity);
        _llRight = findViewById(R.id.ll_right_gravity);
        _llBottom = findViewById(R.id.ll_bottom_gravity);
        MySensorUtil.MySensorListener mySensorListener = new MySensorUtil.MySensorListener() {
            @Override
            public void LightChanged(float[] array) {
            }

            @Override
            public void AccelerometerChanged(float[] array) {
                float x = array[0];
                float y = array[1];
                float z = array[2];
                //重力指向设备左边
                if (x > STANDARD_GRAVITY) {
                    _llLeft.setVisibility(View.VISIBLE);
                }
                //重力指向设备右边
                else if (x < -STANDARD_GRAVITY) {
                    _llRight.setVisibility(View.VISIBLE);
                }
                //重力指向设备下边
                else if (y > STANDARD_GRAVITY) {
                    _llBottom.setVisibility(View.VISIBLE);
                }
                //重力指向设备上边
                else if (y < -STANDARD_GRAVITY) {
                    _llTop.setVisibility(View.VISIBLE);
                }
                //屏幕朝上
                else if (z > STANDARD_GRAVITY) {
                }
                //屏幕朝下
                else if (z < -STANDARD_GRAVITY) {
                }
                JudgePass();
            }

            @Override
            public void MagneticChanged(float[] array) {
            }

            @Override
            public void DirectionChanged(float[] array) {
            }
        };
        _mySensorUtil.Init(this, mySensorListener);
        _mySensorUtil.Register();
    }

}
