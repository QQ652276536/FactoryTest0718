package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.zistone.factorytest0718.util.MySensorUtil;

public class MagneticActivity extends BaseActivity {

    private static final String TAG = "MagneticActivity";

    private MySensorUtil _mySensorUtil;
    private ImageView _imageView;
    private float _lastRotateDegree, flagDegree;
    private int flag;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _mySensorUtil.UnRegister();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_magnetic);
        SetBaseContentView(R.layout.activity_magnetic);
        _imageView = findViewById(R.id.iv_magnetic);
        _btnPass.setEnabled(false);
        _mySensorUtil = MySensorUtil.GetInstance();
        MySensorUtil.MySensorListener mySensorListener = new MySensorUtil.MySensorListener() {
            @Override
            public void LightChanged(float[] array) {
            }

            @Override
            public void AccelerometerChanged(float[] array) {
            }

            @Override
            public void MagneticChanged(float[] array) {
            }

            @Override
            public void DirectionChanged(float[] array) {
                //取值范围是-180到180度
                //+-180表示正南方向，0度表示正北，-90表示正西，+90表示正东
                //将计算出的旋转角度取反，用于旋转指南针背景图
                float rotateDegree = -(float) Math.toDegrees(array[0]);
                //这段判断测试通过的迷之代码来自T1_PCBA项目
                flag++;
                if (flag == 50) {
                    flagDegree = rotateDegree;
                }
                if (flag > 50) {
                    if (flagDegree - rotateDegree < -30 || flagDegree - rotateDegree > 30) {
                        _btnPass.setEnabled(true);
                        Pass();
                    }
                }
                if (Math.abs(rotateDegree - _lastRotateDegree) > 1) {
                    Log.i(TAG, "图片需要旋转的角度：" + rotateDegree);
                    RotateAnimation animation = new RotateAnimation(_lastRotateDegree, rotateDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setFillAfter(true);
                    _imageView.startAnimation(animation);
                    _lastRotateDegree = rotateDegree;
                }
            }
        };
        _mySensorUtil.Init(this, mySensorListener);
        _mySensorUtil.Register();
    }

}
