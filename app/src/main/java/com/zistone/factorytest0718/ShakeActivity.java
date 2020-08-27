package com.zistone.factorytest0718;

import android.os.Bundle;
import android.os.Vibrator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zistone.factorytest0718.util.MyImageUtil;

/**
 * 震动测试
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class ShakeActivity extends BaseActivity {

    private static final String TAG = "ShakeActivity";

    private Vibrator _vibrator;
    private long[] _patter = {0, 500, 500, 0};
    private ImageView _imageView;

    @Override
    protected void onResume() {
        super.onResume();
        _vibrator.vibrate(_patter, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        _vibrator.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_shake);
        SetBaseContentView(R.layout.activity_shake);
        _vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        _imageView = findViewById(R.id.iv_shake);
        Glide.with(this).load(R.drawable.shaking).into(_imageView);
    }
}
